package com.infinum.sentinel.domain.deeplinks.models

import com.infinum.sentinel.domain.deeplinks.uri.DeepLinkUriBuilder
import com.infinum.sentinel.domain.deeplinks.uri.DeepLinkUriEncoder

/**
 * A link the user can start from: either declared in the manifest or given as an example.
 *
 * @property key stable identity that survives app updates, see BaseLinkKeys.
 * @property origin where the link comes from: a declared screen or an integrator example.
 * @property spec the parsed shape. Null only for an example that could not be parsed.
 * @property exampleUri the parsed example, null for declared links and unparseable examples.
 * @property warnings configuration problems found in the manifest (FR-56). Always empty for examples.
 */
internal data class BaseLink(
    val key: String,
    val origin: BaseLinkOrigin,
    val spec: LinkSpec?,
    val exampleUri: DeepLinkUri?,
    val warnings: Set<ConfigurationWarning> = setOf(),
) {
    val type: LinkType
        get() = LinkType.from(spec?.scheme)

    val isTemplate: Boolean
        get() = spec?.isTemplate ?: false

    /** The example text for examples, the declared shape for declared links. */
    val displayText: String
        get() =
            when (origin) {
                is BaseLinkOrigin.Example -> origin.rawUri
                is BaseLinkOrigin.Declared -> spec?.displayText.orEmpty()
            }

    /**
     * The link that can be launched as is. Null for declared templates, which need filling first.
     */
    val launchLink: String?
        get() =
            when (origin) {
                is BaseLinkOrigin.Example -> origin.rawUri
                is BaseLinkOrigin.Declared -> spec?.takeUnless { it.isTemplate }?.let(::buildDeclared)
            }

    /** The link always has the `//` part, so a declared path without a leading slash gets one. */
    private fun buildDeclared(spec: LinkSpec): String =
        DeepLinkUriBuilder.build(
            DeepLinkUri(
                scheme = spec.scheme,
                hasAuthority = true,
                host = (spec.host as? HostSpec.Literal)?.host,
                port = spec.port,
                path =
                DeepLinkUriEncoder.encodePath(
                    LinkSpec.withLeadingSlash((spec.path as? PathSpec.Literal)?.value.orEmpty()),
                ),
                params = listOf(),
                fragment = null,
            ),
        )
}
