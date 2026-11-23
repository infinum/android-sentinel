package com.infinum.sentinel.domain.deeplinks.discovery

import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec

/**
 * Builds the stable keys under which base links are remembered. A key depends only on the
 * declared shape (or the example text), so it survives app updates.
 */
internal object BaseLinkKeys {
    private const val DECLARED_PREFIX = "declared"
    private const val EXAMPLE_PREFIX = "example|"
    private const val SEPARATOR = "|"

    /**
     * Key of a declared link: `declared|scheme|host|port|path`. Scheme and host compare
     * case-insensitively (as in Android's matching), path values are kept verbatim.
     */
    internal fun declared(spec: LinkSpec): String =
        listOf(
            DECLARED_PREFIX,
            spec.scheme.lowercase(),
            hostKey(spec.host),
            spec.port?.toString().orEmpty(),
            pathKey(spec.path),
        ).joinToString(SEPARATOR)

    /**
     * Key of an example link: the raw text with an `example|` prefix.
     */
    internal fun example(rawUri: String): String = EXAMPLE_PREFIX + rawUri

    /**
     * True when [key] is the key of an example link.
     */
    internal fun isExample(key: String): Boolean = key.startsWith(EXAMPLE_PREFIX)

    private fun hostKey(host: HostSpec): String =
        when (host) {
            HostSpec.None -> "none"
            HostSpec.AnyHost -> "any"
            is HostSpec.Literal -> "lit:" + host.host.lowercase()
            is HostSpec.Wildcard -> "wild:" + host.suffix.lowercase()
        }

    private fun pathKey(path: PathSpec): String =
        when (path) {
            PathSpec.None -> "none"
            is PathSpec.Literal -> "lit:" + path.value
            is PathSpec.Prefix -> "prefix:" + path.value
            is PathSpec.Pattern -> "pattern:" + path.value
            is PathSpec.AdvancedPattern -> "adv:" + path.value
            is PathSpec.Suffix -> "suffix:" + path.value
        }
}
