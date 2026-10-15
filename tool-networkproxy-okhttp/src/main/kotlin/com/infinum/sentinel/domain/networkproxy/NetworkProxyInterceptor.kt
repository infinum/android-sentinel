package com.infinum.sentinel.domain.networkproxy

import android.content.Context
import com.infinum.sentinel.domain.networkproxy.models.ProxyRule
import com.infinum.sentinel.domain.networkproxy.models.RecentCall
import com.infinum.sentinel.domain.networkproxy.models.RuleMode
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

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
            RuleMode.MODIFY -> {
                val real = chain.proceed(request)
                // Captured before modify() so the recorded call always holds the real body
                val captured = safely { capture(real) }
                val modified = safely { modify(real, rule) }
                if (captured != null) {
                    safely { recentCalls.record(captured.copy(mocked = modified != null)) }
                }
                modified ?: real
            }
        }
    }

    private fun proceedAndRecord(
        chain: Interceptor.Chain,
        request: Request,
    ): Response = chain.proceed(request).also { real -> safely { capture(real)?.let(recentCalls::record) } }

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

    private fun capture(real: Response): RecentCall {
        val request = real.request
        val contentType = real.body.contentType()
        return RecentCall(
            method = request.method,
            path = request.url.encodedPath,
            statusCode = real.code,
            timestamp = System.currentTimeMillis(),
            body = captureBody(real, contentType),
            contentType = contentType?.toString(),
            mocked = false,
        )
    }

    // Unknown length (-1) is still captured: OkHttp drops Content-Length, so most real API
    // responses report it. Streaming types are excluded by name instead, since peeking one would block the
    // caller until the stream ends
    private fun captureBody(
        response: Response,
        contentType: MediaType?,
    ): String? {
        val capturable =
            contentType != null &&
                contentType.isTextLike() &&
                !contentType.isStreaming() &&
                response.body.contentLength() <= MAX_BODY_BYTES
        if (!capturable) return null
        // Peeking one byte past the limit tells a chunked body apart from one that fits exactly
        val peeked = response.peekBody(MAX_BODY_BYTES + 1)
        return if (peeked.contentLength() > MAX_BODY_BYTES) null else peeked.string()
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
        private val STREAMING_TYPES =
            listOf(
                "text" to "event-stream",
                "application" to "x-ndjson",
                "application" to "stream+json",
            )
    }
}
