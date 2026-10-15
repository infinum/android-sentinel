package com.infinum.sentinel.domain.networkproxy.models

import java.util.UUID

internal data class ProxyRule(
    val id: String = UUID.randomUUID().toString(),
    val enabled: Boolean = true,
    val method: HttpMethod = HttpMethod.GET,
    val pathPattern: String = "/",
    val mode: RuleMode = RuleMode.MODIFY,
    val statusCode: Int = DEFAULT_STATUS_CODE,
    val body: String? = null,
    val contentType: String = DEFAULT_CONTENT_TYPE,
) {
    companion object {
        const val DEFAULT_STATUS_CODE: Int = 200
        const val MIN_STATUS_CODE: Int = 100
        const val MAX_STATUS_CODE: Int = 599
        const val DEFAULT_CONTENT_TYPE: String = "application/json; charset=utf-8"
    }
}
