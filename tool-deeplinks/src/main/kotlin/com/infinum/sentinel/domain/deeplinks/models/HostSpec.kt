package com.infinum.sentinel.domain.deeplinks.models

/**
 * The host part of a declared link, as the manifest declares it.
 */
internal sealed interface HostSpec {
    /** No host declared. Android then ignores any path, port and the host in matching. */
    data object None : HostSpec

    /** Declared as `*`: any host matches. */
    data object AnyHost : HostSpec

    /** A fixed host, kept as declared. */
    data class Literal(val host: String) : HostSpec

    /**
     * Declared with a leading `*`, as in `*.example.com` or `*example.com`: any host that ends in [suffix] matches,
     * the way Android reads it. [suffix] is the text after the `*`, so it keeps the dot of `*.example.com`.
     */
    data class Wildcard(val suffix: String) : HostSpec
}
