package com.infinum.sentinel.domain.deeplinks.models

/**
 * Where a base link comes from.
 */
internal sealed interface BaseLinkOrigin {
    /** Declared by the manifest component named [componentName]. */
    data class Declared(val componentName: String) : BaseLinkOrigin

    /** Supplied by the integrator as a full example link. */
    data class Example(val label: String?, val rawUri: String) : BaseLinkOrigin
}
