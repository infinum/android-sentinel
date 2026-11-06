package com.infinum.sentinel.domain.networkproxy

import android.content.Context
import android.util.Log
import com.infinum.sentinel.domain.networkproxy.models.ProxyRule
import com.infinum.sentinel.domain.networkproxy.models.RecentCall
import com.infinum.sentinel.domain.networkproxy.models.RuleMode
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.buffer

public class NetworkProxyInterceptor internal constructor(
    private val ruleSource: RuleSource,
    private val recentCalls: RecentCallStore,
) : Interceptor {
    public constructor(context: Context) : this(
        ruleSource = NetworkProxyPreferences(context.applicationContext),
        recentCalls = RecentCallStore.shared,
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (chain.connection() != null) {
            Log.w("Sentinel", NETWORK_INTERCEPTOR_WARNING)
            return chain.proceed(request)
        }
        val rule = safely { findRule(request) }
        return when (rule?.mode) {
            null -> proceedAndRecord(chain, request)
            RuleMode.MOCK -> {
                val mocked = safely { mock(request, rule) }
                if (mocked == null) {
                    proceedAndRecord(chain, request)
                } else {
                    safely { recentCalls.markMocked(request.method, request.url.encodedPath, mocked.code) }
                    mocked
                }
            }
            RuleMode.MODIFY -> proceedAndModify(chain, request, rule)
        }
    }

    private fun proceedAndRecord(
        chain: Interceptor.Chain,
        request: Request,
    ): Response = recording(request, chain.proceed(request), mocked = false)

    private fun proceedAndModify(
        chain: Interceptor.Chain,
        request: Request,
        rule: ProxyRule,
    ): Response {
        val real = chain.proceed(request)
        // The app never reads a replaced real body, so it is read here for the recording, before modify() closes it
        val realBody = rule.body?.let { safely { readBody(real) } }
        val modified = safely { modify(real, rule) }
        return when {
            modified == null -> recording(request, real, mocked = false)
            rule.body == null -> recording(request, modified, statusCode = real.code, mocked = true)
            else -> {
                safely { recentCalls.record(recentCall(request, real, real.code, mocked = true).copy(body = realBody)) }
                modified
            }
        }
    }

    private fun findRule(request: Request): ProxyRule? {
        if (!ruleSource.isEnabled) return null
        return RuleMatcher.firstMatch(ruleSource.rules, request.method, request.url.encodedPath)
    }

    private fun mock(
        request: Request,
        rule: ProxyRule,
    ): Response {
        val now = System.currentTimeMillis()
        val bytes = rule.body.orEmpty().toByteArray()
        return Response
            .Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(rule.statusCode)
            .message(HttpReasonPhrases.forCode(rule.statusCode))
            .header(HEADER_CONTENT_TYPE, rule.contentType)
            .header(HEADER_CONTENT_LENGTH, bytes.size.toString())
            .body(bytes.toResponseBody(rule.contentType.toMediaTypeOrNull()))
            .sentRequestAtMillis(now)
            .receivedResponseAtMillis(now)
            .build()
    }

    private fun modify(
        real: Response,
        rule: ProxyRule,
    ): Response {
        val builder =
            real
                .newBuilder()
                .code(rule.statusCode)
                .message(HttpReasonPhrases.forCode(rule.statusCode))
        val body = rule.body
        if (body != null) {
            val bytes = body.toByteArray()
            builder
                .header(HEADER_CONTENT_TYPE, rule.contentType)
                .header(HEADER_CONTENT_LENGTH, bytes.size.toString())
                .body(bytes.toResponseBody(rule.contentType.toMediaTypeOrNull()))
        }
        val modified = builder.build()
        if (body != null) {
            real.body.close()
        }
        return modified
    }

    // The body is captured while the app reads it and never read here, since reading it would hold the response
    // back until the whole body has downloaded
    private fun recording(
        request: Request,
        response: Response,
        statusCode: Int = response.code,
        mocked: Boolean,
    ): Response =
        safely {
            val call = recentCall(request, response, statusCode, mocked)
            val contentType = response.body.contentType()
            if (contentType == null || !isCapturable(response, contentType)) {
                recentCalls.record(call)
                response
            } else {
                val source =
                    CapturingSource(response.body.source(), MAX_BODY_BYTES) { captured ->
                        safely {
                            val body = captured?.readString(contentType.charset() ?: Charsets.UTF_8)
                            recentCalls.record(call.copy(timestamp = System.currentTimeMillis(), body = body))
                        }
                    }
                response
                    .newBuilder()
                    .body(source.buffer().asResponseBody(contentType, response.body.contentLength()))
                    .build()
            }
        } ?: response

    private fun recentCall(
        request: Request,
        response: Response,
        statusCode: Int,
        mocked: Boolean,
    ): RecentCall =
        RecentCall(
            method = request.method,
            path = request.url.encodedPath,
            statusCode = statusCode,
            timestamp = System.currentTimeMillis(),
            body = null,
            contentType = response.body.contentType()?.toString(),
            mocked = mocked,
        )

    // Unknown length (-1) is still captured: OkHttp drops Content-Length, so most real API responses report it.
    // Streaming types are recorded right away without a body, since a stream may never reach its end
    private fun isCapturable(
        response: Response,
        contentType: MediaType,
    ): Boolean = contentType.isTextLike() && !contentType.isStreaming() && response.body.contentLength() <= MAX_BODY_BYTES

    private fun readBody(response: Response): String? {
        val contentType = response.body.contentType()
        if (contentType == null || !isCapturable(response, contentType)) return null
        // Peeking one byte past the limit tells a chunked body apart from one that fits exactly
        val peeked = response.peekBody(MAX_BODY_BYTES + 1)
        return peeked.takeIf { it.contentLength() <= MAX_BODY_BYTES }?.string()
    }

    private fun MediaType.isTextLike(): Boolean =
        type == "text" || subtype.contains("json") || subtype.contains("xml") || subtype == "x-www-form-urlencoded"

    private fun MediaType.isStreaming(): Boolean = STREAMING_TYPES.any { it.first == type && it.second == subtype }

    // The proxy must never be the reason a call fails, so anything thrown by rule lookup, response building
    // or recording is dropped and the real response wins
    @Suppress("SwallowedException")
    private inline fun <T> safely(block: () -> T): T? =
        try {
            block()
        } catch (error: Exception) {
            null
        }

    private companion object {
        private const val MAX_BODY_BYTES: Long = 256L * 1024L
        private const val HEADER_CONTENT_TYPE = "Content-Type"
        private const val HEADER_CONTENT_LENGTH = "Content-Length"
        private const val NETWORK_INTERCEPTOR_WARNING =
            "NetworkProxyInterceptor was added with addNetworkInterceptor(), so its rules are ignored. " +
                "Add it with addInterceptor() instead."
        private val STREAMING_TYPES =
            listOf(
                "text" to "event-stream",
                "application" to "x-ndjson",
                "application" to "stream+json",
            )
    }
}
