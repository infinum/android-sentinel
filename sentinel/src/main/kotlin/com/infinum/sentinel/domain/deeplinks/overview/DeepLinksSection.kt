package com.infinum.sentinel.domain.deeplinks.overview

/**
 * A section of the Deep links list.
 */
internal sealed interface DeepLinksSection {
    /** The example links passed to DeepLinkTool, in the order given (FR-10). */
    data class Examples(
        val entries: List<BaseLinkEntry>,
    ) : DeepLinksSection

    /** The links declared by the screen named [componentName], under the header [title] (FR-8). */
    data class Screen(
        val componentName: String,
        val title: String,
        val entries: List<BaseLinkEntry>,
    ) : DeepLinksSection

    /** Variants whose base link is gone, in storage order (FR-47). */
    data class NoLongerDeclared(
        val variants: List<VariantEntry>,
    ) : DeepLinksSection

    /** Variants of declared links that can't be matched because discovery failed, in storage order (FR-50). */
    data class SavedVariants(
        val variants: List<VariantEntry>,
    ) : DeepLinksSection
}
