package com.infinum.sentinel.ui.deeplinks.breakdown

import com.infinum.sentinel.domain.deeplinks.uri.DeepLinkUriEncoder
import com.infinum.sentinel.domain.deeplinks.uri.DeepLinkUriParser

/**
 * Splits a link string into a [LinkBreakdown].
 */
internal object LinkBreakdownMapper {
    /**
     * Returns [LinkBreakdown.Parsed] with decoded path and fragment, or [LinkBreakdown.Unparseable]
     * when [link] has no scheme.
     */
    internal fun map(link: String): LinkBreakdown =
        DeepLinkUriParser.parse(link)?.let { uri ->
            LinkBreakdown.Parsed(
                fullLink = link,
                scheme = uri.scheme,
                host = uri.host,
                port = uri.port,
                path = DeepLinkUriEncoder.decodePath(uri.path),
                params = uri.params,
                fragment = uri.fragment?.let { DeepLinkUriEncoder.decodePath(it) },
            )
        } ?: LinkBreakdown.Unparseable(link)
}
