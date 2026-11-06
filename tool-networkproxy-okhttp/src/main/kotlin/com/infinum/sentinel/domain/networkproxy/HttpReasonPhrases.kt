package com.infinum.sentinel.domain.networkproxy

@Suppress("MagicNumber")
internal object HttpReasonPhrases {
    private val phrases: Map<Int, String> =
        mapOf(
            100 to "Continue",
            101 to "Switching Protocols",
            200 to "OK",
            201 to "Created",
            202 to "Accepted",
            204 to "No Content",
            206 to "Partial Content",
            301 to "Moved Permanently",
            302 to "Found",
            303 to "See Other",
            304 to "Not Modified",
            307 to "Temporary Redirect",
            308 to "Permanent Redirect",
            400 to "Bad Request",
            401 to "Unauthorized",
            402 to "Payment Required",
            403 to "Forbidden",
            404 to "Not Found",
            405 to "Method Not Allowed",
            406 to "Not Acceptable",
            408 to "Request Timeout",
            409 to "Conflict",
            410 to "Gone",
            412 to "Precondition Failed",
            413 to "Payload Too Large",
            415 to "Unsupported Media Type",
            422 to "Unprocessable Entity",
            429 to "Too Many Requests",
            500 to "Internal Server Error",
            501 to "Not Implemented",
            502 to "Bad Gateway",
            503 to "Service Unavailable",
            504 to "Gateway Timeout",
        )

    fun forCode(code: Int): String = phrases[code] ?: "Unknown"
}
