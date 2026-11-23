package com.infinum.sentinel.ui.deeplinks.list

import androidx.annotation.StringRes
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelItemDeepLinkBinding
import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.LinkType
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import com.infinum.sentinel.ui.deeplinks.verification.VerificationStatusDialogFragment

/**
 * Shows a base link: example label, link text, chips, variant count and actions (FR-9, FR-17, FR-56).
 */
internal class BaseLinkViewHolder(
    private val binding: SentinelItemDeepLinkBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        row: DeepLinksItem.BaseLinkRow,
        listener: DeepLinksListener,
    ) {
        with(binding) {
            titleView.text = row.title
            titleView.isVisible = row.title != null
            linkView.text = row.baseLink.displayText
            variantCountView.text = variantSummary(row)
            launchButton.isVisible = row.launchLink != null
            addVariantButton.isEnabled = row.baseLink.spec != null
            root.setOnClickListener { listener.onRowClick(row) }
            launchButton.setOnClickListener { listener.onLaunchClick(row, it) }
            moreButton.setOnClickListener { listener.onMoreClick(row, it) }
            addVariantButton.setOnClickListener { listener.onAddVariantClick(row) }
        }
        bindChips(row.baseLink)
        bindStatusChips(row.status, binding.statusChip, binding.statusWarningChip) { listener.onStatusClick(row) }
        binding.chipGroup.isVisible = binding.chipGroup.children.any { it.isVisible }
    }

    private fun bindChips(baseLink: BaseLink) {
        val duplicate = baseLink.warnings.filterIsInstance<ConfigurationWarning.DuplicateDeclaration>().firstOrNull()
        with(binding) {
            typeChip.isVisible = baseLink.spec != null
            typeChip.setText(typeLabel(baseLink.type))
            templateChip.isVisible = baseLink.isTemplate
            missingDefaultChip.isVisible = ConfigurationWarning.MissingDefault in baseLink.warnings
            missingBrowsableChip.isVisible = ConfigurationWarning.MissingBrowsable in baseLink.warnings
            duplicateChip.isVisible = duplicate != null
            duplicate?.let { warning ->
                duplicateChip.text =
                    root.context.getString(
                        R.string.sentinel_deep_links_warning_duplicate,
                        warning.otherScreens.joinToString(", "),
                    )
            }
            chipGroup.isVisible = chipGroup.children.any { it.isVisible }
        }
    }

    /**
     * The variant count, or for a template without variants a hint that a variant is how to launch it.
     * Nothing when there are no variants otherwise, since "0 variants" on every row is noise.
     */
    private fun variantSummary(row: DeepLinksItem.BaseLinkRow): String? {
        val resources = binding.root.resources
        return when {
            row.variantCount > 0 ->
                resources.getQuantityString(R.plurals.sentinel_deep_links_variant_count, row.variantCount, row.variantCount)

            row.baseLink.isTemplate -> resources.getString(R.string.sentinel_deep_links_template_hint)
            else -> null
        }
    }

    @StringRes
    private fun typeLabel(type: LinkType): Int =
        when (type) {
            LinkType.CUSTOM_SCHEME -> R.string.sentinel_deep_links_custom_scheme
            LinkType.WEB -> R.string.sentinel_deep_links_web
        }
}

/**
 * Shows [status] on one of two chips: [labelChip] for a good status, [warningChip] for a bad one. Neither shows
 * when [status] is null. The text carries the meaning, so color is never the only signal (PRD section 6).
 */
internal fun bindStatusChips(
    status: VerificationStatus?,
    labelChip: Chip,
    warningChip: Chip,
    onClick: () -> Unit,
) {
    val isWarning = status == VerificationStatus.NOT_VERIFIED || status == VerificationStatus.NOT_REGISTERED
    bindStatusChip(labelChip, status.takeUnless { isWarning }, onClick)
    bindStatusChip(warningChip, status.takeIf { isWarning }, onClick)
}

private fun bindStatusChip(
    chip: Chip,
    status: VerificationStatus?,
    onClick: () -> Unit,
) {
    chip.isVisible = status != null
    if (status != null) {
        val label = chip.context.getString(VerificationStatusDialogFragment.labelOf(status))
        chip.text = label
        chip.contentDescription = chip.context.getString(R.string.sentinel_deep_links_status_content_description, label)
        chip.setOnClickListener { onClick() }
    } else {
        chip.setOnClickListener(null)
        chip.isClickable = false
    }
}
