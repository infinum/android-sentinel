package com.infinum.sentinel.data.models.local.deeplinks

import kotlinx.serialization.Serializable

/**
 * The stored document. There are no defaults, so [version] is always written.
 */
@Serializable
internal data class DeepLinkVariantsDocumentEntity(
    val version: Int,
    val variants: List<DeepLinkVariantEntity>,
)
