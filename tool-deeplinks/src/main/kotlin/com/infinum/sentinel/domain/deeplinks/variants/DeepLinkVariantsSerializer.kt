package com.infinum.sentinel.domain.deeplinks.variants

import com.infinum.sentinel.data.models.local.deeplinks.DeepLinkQueryParamEntity
import com.infinum.sentinel.data.models.local.deeplinks.DeepLinkVariantEntity
import com.infinum.sentinel.data.models.local.deeplinks.DeepLinkVariantsDocumentEntity
import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import com.infinum.sentinel.domain.deeplinks.models.Variant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Converts variants to and from the versioned JSON document kept in storage.
 */
internal object DeepLinkVariantsSerializer {
    /**
     * The document version written by [encode].
     */
    const val CURRENT_VERSION = 1

    private val format = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    /**
     * Encodes [variants] as a document with [CURRENT_VERSION].
     */
    fun encode(variants: List<Variant>): String =
        format.encodeToString(
            DeepLinkVariantsDocumentEntity(
                version = CURRENT_VERSION,
                variants = variants.map { it.toEntity() },
            ),
        )

    /**
     * Decodes a stored document. Returns null on any failure, and when the document was written by a newer schema
     * ([CURRENT_VERSION] exceeded), so the caller can back the raw document up instead of losing data.
     */
    fun decode(json: String): List<Variant>? =
        try {
            val document = format.decodeFromString<DeepLinkVariantsDocumentEntity>(json)
            if (document.version > CURRENT_VERSION) {
                null
            } else {
                document.variants.map { it.toVariant() }
            }
        } catch (ignored: SerializationException) {
            null
        } catch (ignored: IllegalArgumentException) {
            null
        }

    private fun Variant.toEntity(): DeepLinkVariantEntity =
        DeepLinkVariantEntity(
            id = id,
            name = name,
            baseKey = baseKey,
            componentName = componentName,
            scheme = uri.scheme,
            hasAuthority = uri.hasAuthority,
            host = uri.host,
            port = uri.port,
            path = uri.path,
            fragment = uri.fragment,
            params = uri.params.map { DeepLinkQueryParamEntity(key = it.key, value = it.value) },
        )

    private fun DeepLinkVariantEntity.toVariant(): Variant =
        Variant(
            id = id,
            name = name,
            baseKey = baseKey,
            componentName = componentName,
            uri = DeepLinkUri(
                scheme = scheme,
                hasAuthority = hasAuthority,
                host = host,
                port = port,
                path = path,
                params = params.map { QueryParam(key = it.key, value = it.value) },
                fragment = fragment,
            ),
        )
}
