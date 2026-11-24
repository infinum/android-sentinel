package com.infinum.sentinel.ui.deeplinks.list

import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.Variant
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import com.infinum.sentinel.domain.deeplinks.overview.BaseLinkEntry
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksNotice
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksOverview
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksSection
import com.infinum.sentinel.domain.deeplinks.overview.VariantEntry
import com.infinum.sentinel.domain.deeplinks.uri.DeepLinkUriBuilder

/**
 * One row of the Deep links list. Items are immutable because the list diffs them on a background thread.
 */
internal sealed interface DeepLinksItem {
    /** A notice shown above every section. */
    data class Notice(
        val notice: DeepLinksNotice,
    ) : DeepLinksItem

    /**
     * The header of a section with a fixed title: Example links, No longer declared or Saved variants. [sourceRes]
     * says where the section's links come from, when that isn't clear from the title.
     */
    data class SectionHeader(
        @StringRes val titleRes: Int,
        @StringRes val sourceRes: Int? = null,
    ) : DeepLinksItem

    /** The header of the section of the screen named [componentName]. */
    data class ScreenHeader(
        val componentName: String,
        val title: String,
    ) : DeepLinksItem

    /**
     * A row that stands for one link, a base link or a variant. Callbacks receive it whole.
     */
    sealed interface LinkRow : DeepLinksItem {
        /** The example label or the variant name. Null when there is none or it is blank. */
        val title: String?

        /** The link a breakdown opens. For a declared template it is the pattern text. */
        val link: String

        /** The link Launch, Copy and Share use. Null only for a declared template, which can't be launched. */
        val launchLink: String?

        /** The App Links verification status of the row's host. Null when there is none (FR-38, FR-44). */
        val status: VerificationStatus?
    }

    /** A base link with the number of variants attached to it. [link] is [launchLink], or the display text of a template. */
    data class BaseLinkRow(
        val baseLink: BaseLink,
        val variantCount: Int,
        override val title: String?,
        override val link: String,
        override val launchLink: String?,
        override val status: VerificationStatus?,
    ) : LinkRow

    /**
     * A variant and its built link. [baseLink] is null in the No longer declared and Saved variants sections,
     * and [isOrphan] is true only in No longer declared.
     */
    data class VariantRow(
        val variant: Variant,
        val baseLink: BaseLink?,
        val isOrphan: Boolean,
        override val title: String?,
        override val link: String,
        override val status: VerificationStatus?,
    ) : LinkRow {
        override val launchLink: String
            get() = link
    }
}

/**
 * Flattens the overview into list items: notices first, then each section's header followed by its rows.
 * Variant links are built here once, so binding and callbacks never rebuild them.
 */
internal fun DeepLinksOverview.flatten(): List<DeepLinksItem> =
    buildList<DeepLinksItem> {
        notices.forEach { add(DeepLinksItem.Notice(it)) }
        sections.forEach { addSection(it) }
    }

private fun MutableList<DeepLinksItem>.addSection(section: DeepLinksSection) {
    when (section) {
        is DeepLinksSection.Examples -> {
            add(DeepLinksItem.SectionHeader(R.string.sentinel_deep_links_examples, R.string.sentinel_deep_links_source_examples))
            section.entries.forEach { addEntry(it) }
        }

        is DeepLinksSection.Screen -> {
            add(DeepLinksItem.ScreenHeader(section.componentName, section.title))
            section.entries.forEach { addEntry(it) }
        }

        is DeepLinksSection.NoLongerDeclared -> {
            add(DeepLinksItem.SectionHeader(R.string.sentinel_deep_links_no_longer_declared))
            section.variants.forEach { add(variantRow(it, baseLink = null, isOrphan = true)) }
        }

        is DeepLinksSection.SavedVariants -> {
            add(DeepLinksItem.SectionHeader(R.string.sentinel_deep_links_saved_variants))
            section.variants.forEach { add(variantRow(it, baseLink = null, isOrphan = false)) }
        }
    }
}

private fun MutableList<DeepLinksItem>.addEntry(entry: BaseLinkEntry) {
    add(baseLinkRow(entry))
    entry.variants.forEach { add(variantRow(it, baseLink = entry.baseLink, isOrphan = false)) }
}

private fun baseLinkRow(entry: BaseLinkEntry): DeepLinksItem.BaseLinkRow {
    val launchLink = entry.baseLink.launchLink
    return DeepLinksItem.BaseLinkRow(
        baseLink = entry.baseLink,
        variantCount = entry.variantCount,
        title = (entry.baseLink.origin as? BaseLinkOrigin.Example)?.label?.takeUnless { it.isBlank() },
        link = launchLink ?: entry.baseLink.displayText,
        launchLink = launchLink,
        status = entry.status,
    )
}

private fun variantRow(
    entry: VariantEntry,
    baseLink: BaseLink?,
    isOrphan: Boolean,
): DeepLinksItem.VariantRow {
    val variant = entry.variant
    return DeepLinksItem.VariantRow(
        variant = variant,
        baseLink = baseLink,
        isOrphan = isOrphan,
        title = variant.name?.takeUnless { it.isBlank() },
        link = DeepLinkUriBuilder.build(variant.uri),
        status = entry.status,
    )
}
