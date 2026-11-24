package com.infinum.sentinel.domain.deeplinks.discovery

import android.os.Build
import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.DeclaredData
import com.infinum.sentinel.domain.deeplinks.models.DeclaredFilter
import com.infinum.sentinel.domain.deeplinks.models.DeclaredScreen
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec

/**
 * Expands declared intent filters into base links grouped by screen, the way Android merges the
 * data elements of one filter: every scheme combines with every authority and every path.
 */
internal object BaseLinkExpander {
    private const val WILDCARD = "*"

    private data class Authority(
        val host: HostSpec,
        val port: Int?,
    )

    /**
     * Builds the screens for [filters] on a device running [sdkInt].
     *
     * Screens keep first-appearance order. When one screen declares the same link more than once,
     * the first position is kept and the warnings are merged by capability, see [mergeWarnings].
     */
    internal fun expand(
        filters: List<DeclaredFilter>,
        sdkInt: Int,
    ): List<DeclaredScreen> =
        filters
            .groupBy { it.componentName }
            .map { (componentName, componentFilters) ->
                DeclaredScreen(
                    componentName = componentName,
                    title = componentFilters.first().title,
                    links = mergeSameKey(componentFilters.flatMap { expandFilter(it, sdkInt) }),
                )
            }.filter { it.links.isNotEmpty() }

    /**
     * Mirrors IntentFilter.matchData: a filter without any authority (host) ignores its paths and
     * ports, so only the bare scheme is a link. Otherwise every scheme, authority and path combine.
     */
    private fun expandFilter(
        filter: DeclaredFilter,
        sdkInt: Int,
    ): List<BaseLink> {
        val schemes = filter.data.mapNotNull { it.scheme }.distinct()
        val authorities = authorities(filter.data)
        val paths = paths(filter, sdkInt)
        val warnings = ConfigurationWarnings.categoryWarnings(filter.categories)
        val specs =
            when {
                authorities.isEmpty() -> schemes.map { LinkSpec(it, HostSpec.None, null, PathSpec.None) }
                paths.isEmpty() ->
                    schemes.flatMap { scheme ->
                        authorities.map { LinkSpec(scheme, it.host, it.port, PathSpec.None) }
                    }
                else ->
                    schemes.flatMap { scheme ->
                        authorities.flatMap { authority ->
                            paths.map { LinkSpec(scheme, authority.host, authority.port, it) }
                        }
                    }
            }
        return specs.map { spec ->
            BaseLink(
                key = BaseLinkKeys.declared(spec),
                origin = BaseLinkOrigin.Declared(filter.componentName),
                spec = spec,
                exampleUri = null,
                warnings = warnings,
            )
        }
    }

    /**
     * Mirrors IntentFilter.addDataAuthority: one authority per data element that has a host.
     * A port without a host adds nothing.
     */
    private fun authorities(data: List<DeclaredData>): List<Authority> =
        data
            .mapNotNull { item -> item.host?.let { Authority(hostSpec(it), item.port?.toIntOrNull()) } }
            .distinct()

    private fun hostSpec(host: String): HostSpec =
        when {
            host == WILDCARD -> HostSpec.AnyHost
            host.startsWith(WILDCARD) -> HostSpec.Wildcard(host.removePrefix(WILDCARD))
            else -> HostSpec.Literal(host)
        }

    /**
     * Paths of the direct data elements, plus (Android 15+) those of groups that allow.
     * Groups that disallow add nothing; their query and fragment rules are ignored.
     */
    private fun paths(
        filter: DeclaredFilter,
        sdkInt: Int,
    ): List<PathSpec> {
        val direct = filter.data.flatMap { pathSpecs(it, sdkInt) }
        val grouped =
            if (sdkInt >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                filter.groups.filter { it.isAllowed }.flatMap { group -> group.data.flatMap { pathSpecs(it, sdkInt) } }
            } else {
                listOf()
            }
        return (direct + grouped).distinct()
    }

    /**
     * Mirrors the PatternMatcher types the OS knows: `pathAdvancedPattern` and `pathSuffix`
     * exist only from Android 12 (S), older versions ignore them.
     */
    private fun pathSpecs(
        data: DeclaredData,
        sdkInt: Int,
    ): List<PathSpec> {
        val supportsAdvanced = sdkInt >= Build.VERSION_CODES.S
        return listOfNotNull(
            data.path?.let { PathSpec.Literal(it) },
            data.pathPrefix?.let { PathSpec.Prefix(it) },
            data.pathPattern?.let { PathSpec.Pattern(it) },
            data.pathAdvancedPattern?.takeIf { supportsAdvanced }?.let { PathSpec.AdvancedPattern(it) },
            data.pathSuffix?.takeIf { supportsAdvanced }?.let { PathSpec.Suffix(it) },
        )
    }

    private fun mergeSameKey(links: List<BaseLink>): List<BaseLink> =
        links
            .groupBy { it.key }
            .values
            .map { sameKey ->
                sameKey.first().copy(warnings = mergeWarnings(sameKey.map { it.warnings }))
            }

    /**
     * Merges the warnings of declarations of one link. Open in this app needs DEFAULT, and Real tap needs
     * DEFAULT and BROWSABLE in the same filter, so a plain intersection would hide a real problem:
     * - a declaration without warnings makes the link work fully, so nothing remains;
     * - otherwise a declaration with DEFAULT makes Open in this app work, so only the missing BROWSABLE remains;
     * - otherwise no declaration has DEFAULT, and the warnings every declaration shares remain.
     */
    private fun mergeWarnings(declarations: List<Set<ConfigurationWarning>>): Set<ConfigurationWarning> =
        when {
            declarations.any { it.isEmpty() } -> setOf()
            declarations.any { ConfigurationWarning.MissingDefault !in it } -> setOf(ConfigurationWarning.MissingBrowsable)
            else -> declarations.reduce { left, right -> left intersect right }
        }
}
