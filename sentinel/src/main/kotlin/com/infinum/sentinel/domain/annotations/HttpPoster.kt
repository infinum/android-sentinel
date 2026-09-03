package com.infinum.sentinel.domain.annotations

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class HttpResponse(
    val code: Int,
    val body: String,
)

/**
 * Minimal POST seam. Exists so exporters can be tested without a network, and so
 * Sentinel needs no HTTP client dependency.
 *
 * Deliberately left behind in `domain.annotations` when the public annotation API moved to
 * `com.infinum.sentinel.annotations`: this seam is internal and must stay that way, and a
 * package that holds nothing but internals cannot be frozen into the published API by accident.
 */
internal fun interface HttpPoster {
    suspend fun post(url: String, headers: Map<String, String>, body: String): HttpResponse
}

internal class UrlConnectionHttpPoster : HttpPoster {
    private companion object {
        private const val CONNECT_TIMEOUT_MILLIS = 15_000
        private const val READ_TIMEOUT_MILLIS = 30_000
        private const val SUCCESS_CODE_MIN = 200
        private const val SUCCESS_CODE_MAX = 299
    }

    override suspend fun post(url: String, headers: Map<String, String>, body: String): HttpResponse =
        withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
                connection.readTimeout = READ_TIMEOUT_MILLIS
                connection.doOutput = true
                headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }

                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                val stream = if (code in SUCCESS_CODE_MIN..SUCCESS_CODE_MAX) connection.inputStream else connection.errorStream
                val responseBody = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                HttpResponse(code, responseBody)
            } finally {
                connection.disconnect()
            }
        }
}
