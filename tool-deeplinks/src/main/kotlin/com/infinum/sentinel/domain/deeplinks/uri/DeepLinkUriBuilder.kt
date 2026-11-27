package com.infinum.sentinel.domain.deeplinks.uri

import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri

/**
 * Builds a deep link string from its parts.
 */
internal object DeepLinkUriBuilder {
    /**
     * Joins the parts of [uri] into a link. Path, fragment and user info are used as-is (already encoded),
     * query keys and values are encoded. Parameter order, duplicates and empty values are preserved.
     */
    internal fun build(uri: DeepLinkUri): String =
        buildString {
            append(uri.scheme).append(':')
            if (uri.hasAuthority) {
                append("//")
                uri.userInfo?.let { append(it).append('@') }
                append(uri.host.orEmpty())
                uri.port?.let { append(':').append(it) }
            }
            append(uri.path)
            if (uri.params.isNotEmpty()) {
                append('?')
                append(
                    uri.params.joinToString("&") {
                        "${DeepLinkUriEncoder.encodeQueryComponent(it.key)}=${DeepLinkUriEncoder.encodeQueryComponent(it.value)}"
                    },
                )
            }
            uri.fragment?.let { append('#').append(it) }
        }
}
