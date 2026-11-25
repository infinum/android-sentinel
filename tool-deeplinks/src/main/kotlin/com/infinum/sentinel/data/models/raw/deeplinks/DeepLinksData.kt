package com.infinum.sentinel.data.models.raw.deeplinks

import com.infinum.sentinel.domain.deeplinks.models.ExampleLinkData

/**
 * Raw input for deep link discovery: the manifest read outcome and the integrator's example links.
 */
internal data class DeepLinksData(
    val manifest: ManifestReadResult,
    val examples: List<ExampleLinkData>,
)
