package com.infinum.sentinel.domain.networkproxy.models

internal enum class HttpMethod {
    ANY,
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
    OPTIONS,
    ;

    fun matches(requestMethod: String): Boolean = this == ANY || name.equals(requestMethod, ignoreCase = true)

    companion object {
        fun fromName(name: String?): HttpMethod = entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: ANY
    }
}
