package com.infinum.sentinel.domain.deeplinks.models

/**
 * The URI related attributes of one `data` element, as written in the manifest.
 */
internal data class DeclaredData(
    val scheme: String? = null,
    val host: String? = null,
    val port: String? = null,
    val path: String? = null,
    val pathPrefix: String? = null,
    val pathPattern: String? = null,
    val pathAdvancedPattern: String? = null,
    val pathSuffix: String? = null,
)
