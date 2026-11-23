package com.infinum.sentinel.domain.deeplinks.models

/**
 * A deep link split into its parts.
 *
 * [path] and [fragment] are stored percent-encoded, exactly as they appear in the link.
 * [params] hold decoded plain text and are encoded again when the link is built.
 * [hasAuthority] is true when the link contains the `//` part, even if the host is empty.
 */
internal data class DeepLinkUri(
    val scheme: String,
    val hasAuthority: Boolean,
    val host: String?,
    val port: Int?,
    val path: String,
    val params: List<QueryParam>,
    val fragment: String?,
)
