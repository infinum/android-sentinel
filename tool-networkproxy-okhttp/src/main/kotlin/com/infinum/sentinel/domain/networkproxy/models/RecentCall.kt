package com.infinum.sentinel.domain.networkproxy.models

internal data class RecentCall(
    val method: String,
    val path: String,
    val statusCode: Int,
    val timestamp: Long,
    val body: String?,
    val contentType: String?,
    val mocked: Boolean,
)
