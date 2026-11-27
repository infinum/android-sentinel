package com.infinum.sentinel.domain.deeplinks.models

/**
 * What the Deep links tool found.
 *
 * @property examples the integrator's example links.
 * @property screens the screens that declare links in the manifest.
 * @property isDiscoveryFailed true when the manifest could not be read or processed.
 */
internal data class DeepLinksDiscovery(
    val examples: List<BaseLink>,
    val screens: List<DeclaredScreen>,
    val isDiscoveryFailed: Boolean,
)
