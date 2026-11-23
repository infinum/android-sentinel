package com.infinum.sentinel.domain.deeplinks.models

/**
 * A tester-made variant of a deep link.
 *
 * It holds fully resolved parts ([uri] and [componentName]), so it can launch without its base link (FR-47).
 * [baseKey] is the stable identity of the base link it was made from.
 */
internal data class Variant(
    val id: String,
    val name: String?,
    val baseKey: String,
    val componentName: String?,
    val uri: DeepLinkUri,
)
