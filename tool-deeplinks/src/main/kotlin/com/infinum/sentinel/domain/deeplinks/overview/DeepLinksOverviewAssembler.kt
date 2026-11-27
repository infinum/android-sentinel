package com.infinum.sentinel.domain.deeplinks.overview

import com.infinum.sentinel.data.models.raw.deeplinks.DomainVerificationData
import com.infinum.sentinel.domain.deeplinks.discovery.BaseLinkKeys
import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.DeepLinksDiscovery
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.Variant
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import com.infinum.sentinel.domain.deeplinks.uri.DeepLinkUriEncoder
import com.infinum.sentinel.domain.deeplinks.verification.VerificationStatusResolver

/**
 * Builds the Deep links list from the discovery result and the saved variants.
 *
 * A variant attaches to the base link with its key in the screen it was saved from. When that screen no
 * longer declares the key, the variant attaches to the first screen, in manifest order, that does. An example
 * variant attaches to the example with its key. A variant that attaches nowhere is no longer declared, except
 * a declared variant while discovery failed: its link may still exist, so it is listed under saved variants.
 */
internal object DeepLinksOverviewAssembler {
    /**
     * Assembles the overview. [variants] are in storage order, and every list in the result keeps that order.
     * [verification] adds a status to every web link and a notice when link handling is off. Null adds neither.
     */
    internal fun assemble(
        discovery: DeepLinksDiscovery,
        variants: List<Variant>,
        verification: DomainVerificationData? = null,
    ): DeepLinksOverview {
        val slotsByKey = slots(discovery).groupBy { it.key }
        // The null key collects the variants that attach to no base link.
        val bySlot = variants.groupBy { slotOf(it, slotsByKey) }
        val (noLongerDeclared, saved) =
            bySlot[null].orEmpty().partition { !discovery.isDiscoveryFailed || BaseLinkKeys.isExample(it.baseKey) }
        val isEmpty =
            !discovery.isDiscoveryFailed &&
                discovery.examples.isEmpty() &&
                discovery.screens.isEmpty() &&
                variants.isEmpty()
        return DeepLinksOverview(
            notices = notices(discovery, verification),
            sections = sections(discovery, bySlot, noLongerDeclared, saved, verification),
            isEmpty = isEmpty,
        )
    }

    private fun slots(discovery: DeepLinksDiscovery): List<Slot> =
        discovery.examples.map { Slot(null, it.key) } +
            discovery.screens.flatMap { screen -> screen.links.map { Slot(screen.componentName, it.key) } }

    private fun slotOf(
        variant: Variant,
        slotsByKey: Map<String, List<Slot>>,
    ): Slot? {
        val candidates = slotsByKey[variant.baseKey].orEmpty()
        return candidates.firstOrNull { it.componentName == variant.componentName } ?: candidates.firstOrNull()
    }

    private fun notices(
        discovery: DeepLinksDiscovery,
        verification: DomainVerificationData?,
    ): List<DeepLinksNotice> =
        buildList {
            if (verification != null && !verification.isLinkHandlingAllowed) add(DeepLinksNotice.LINK_HANDLING_OFF)
            if (discovery.isDiscoveryFailed) add(DeepLinksNotice.DISCOVERY_FAILED)
        }

    private fun sections(
        discovery: DeepLinksDiscovery,
        bySlot: Map<Slot?, List<Variant>>,
        noLongerDeclared: List<Variant>,
        saved: List<Variant>,
        verification: DomainVerificationData?,
    ): List<DeepLinksSection> =
        buildList<DeepLinksSection> {
            if (discovery.examples.isNotEmpty()) {
                add(DeepLinksSection.Examples(discovery.examples.map { entry(it, null, bySlot, verification) }))
            }
            discovery.screens.forEach { screen ->
                val entries = screen.links.map { entry(it, screen.componentName, bySlot, verification) }
                add(DeepLinksSection.Screen(screen.componentName, screen.title, entries))
            }
            if (noLongerDeclared.isNotEmpty()) add(DeepLinksSection.NoLongerDeclared(variantEntries(noLongerDeclared, verification)))
            if (saved.isNotEmpty()) add(DeepLinksSection.SavedVariants(variantEntries(saved, verification)))
        }

    private fun entry(
        link: BaseLink,
        componentName: String?,
        bySlot: Map<Slot?, List<Variant>>,
        verification: DomainVerificationData?,
    ): BaseLinkEntry =
        BaseLinkEntry(
            baseLink = link,
            variants = variantEntries(bySlot[Slot(componentName, link.key)].orEmpty(), verification),
            status = linkStatus(link, verification),
        )

    private fun variantEntries(
        variants: List<Variant>,
        verification: DomainVerificationData?,
    ): List<VariantEntry> =
        variants.map {
            VariantEntry(it, VerificationStatusResolver.resolve(it.uri.scheme, it.uri.host?.let(::linkHost), verification))
        }

    /**
     * The host of a link as Android's `Uri.getHost()` returns it, with percent escapes decoded. Declared hosts come from
     * the manifest, where Android doesn't decode them.
     */
    private fun linkHost(host: String): String = DeepLinkUriEncoder.decodeComponent(host, plusAsSpace = false)

    /**
     * A wildcard host looks up its own declared host, `*` and the suffix. Any host and no host have no status to look up.
     * An example's host is written in a link, so it is decoded first.
     */
    private fun linkStatus(
        link: BaseLink,
        verification: DomainVerificationData?,
    ): VerificationStatus? =
        link.spec?.let { spec ->
            val host =
                when (val hostSpec = spec.host) {
                    is HostSpec.Literal -> if (link.origin is BaseLinkOrigin.Example) linkHost(hostSpec.host) else hostSpec.host
                    is HostSpec.Wildcard -> "*${hostSpec.suffix}"
                    HostSpec.AnyHost, HostSpec.None -> null
                }
            VerificationStatusResolver.resolve(spec.scheme, host, verification)
        }

    /** One base link row: the declaring screen (null for an example) and the link key. */
    private data class Slot(
        val componentName: String?,
        val key: String,
    )
}
