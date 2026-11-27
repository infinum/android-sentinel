package com.infinum.sentinel.domain.deeplinks.models

/**
 * An example link supplied by the integrator, with an optional [label].
 */
internal data class ExampleLinkData(
    val uri: String,
    val label: String?,
)
