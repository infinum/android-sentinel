package com.infinum.sentinel.domain.deeplinks.uri

import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.QueryParam

/**
 * Splits a deep link string into its parts.
 */
internal object DeepLinkUriParser {
    private const val GROUP_SCHEME = 2
    private const val GROUP_AUTHORITY_PART = 3
    private const val GROUP_AUTHORITY = 4
    private const val GROUP_PATH = 5
    private const val GROUP_QUERY = 7
    private const val GROUP_FRAGMENT = 9

    private val URI_REGEX = Regex("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?")

    /**
     * Parses [raw] with the RFC 3986 appendix B expression. Returns null when there is no scheme.
     * Query keys and values are decoded (`+` reads as a space); path, fragment and user info stay raw.
     */
    internal fun parse(raw: String): DeepLinkUri? {
        val groups = URI_REGEX.matchEntire(raw)?.groups
        val scheme = groups?.get(GROUP_SCHEME)?.value
        return if (groups == null || scheme == null) {
            null
        } else {
            val authority = groups[GROUP_AUTHORITY]?.value.orEmpty()
            val (host, port) = parseHostAndPort(authority.substringAfterLast('@'))
            DeepLinkUri(
                scheme = scheme,
                hasAuthority = groups[GROUP_AUTHORITY_PART] != null,
                host = host,
                port = port,
                path = groups[GROUP_PATH]?.value.orEmpty(),
                params = parseQuery(groups[GROUP_QUERY]?.value),
                fragment = groups[GROUP_FRAGMENT]?.value,
                userInfo = authority.substringBeforeLast('@', missingDelimiterValue = "").ifEmpty { null },
            )
        }
    }

    private fun parseHostAndPort(hostAndPort: String): Pair<String?, Int?> {
        val portText = hostAndPort.substringAfterLast(':', "")
        val port = if (portText.isNotEmpty() && portText.all { it in '0'..'9' }) portText.toIntOrNull() else null
        val host = if (port == null) hostAndPort else hostAndPort.substringBeforeLast(':')
        return host.ifEmpty { null } to port
    }

    private fun parseQuery(query: String?): List<QueryParam> =
        query
            .orEmpty()
            .split('&')
            .filter { it.isNotEmpty() }
            .map { segment ->
                QueryParam(
                    key = DeepLinkUriEncoder.decodeComponent(segment.substringBefore('='), plusAsSpace = true),
                    value = DeepLinkUriEncoder.decodeComponent(segment.substringAfter('=', ""), plusAsSpace = true),
                )
            }
}
