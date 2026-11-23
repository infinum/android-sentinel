package com.infinum.sentinel.data.models.raw.deeplinks

/**
 * One element of the host app's compiled AndroidManifest.xml.
 *
 * @property name the tag name, for example `activity` or `data`.
 * @property attributes attribute names (without namespace) mapped to their values.
 * References such as `@string/host` are already resolved to their final text.
 * @property children kept child elements in manifest order.
 */
internal data class ManifestNodeData(
    val name: String,
    val attributes: Map<String, String> = mapOf(),
    val children: List<ManifestNodeData> = listOf(),
)
