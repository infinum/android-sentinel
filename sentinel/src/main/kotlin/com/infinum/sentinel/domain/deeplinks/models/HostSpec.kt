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

    /** Declared as `*.suffix`: [suffix] is the text after `*.`. */
    data class Wildcard(val suffix: String) : HostSpec
}
