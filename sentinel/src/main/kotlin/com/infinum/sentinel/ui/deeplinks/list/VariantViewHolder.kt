package com.infinum.sentinel.ui.deeplinks.list

import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.databinding.SentinelItemDeepLinkVariantBinding

/**
 * Shows a variant: its name, built link, orphan chip and actions (FR-18, FR-19, FR-47).
 */
internal class VariantViewHolder(
    private val binding: SentinelItemDeepLinkVariantBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        row: DeepLinksItem.VariantRow,
        listener: DeepLinksListener,
    ) {
        with(binding) {
            titleView.text = row.title
            titleView.isVisible = row.title != null
            linkView.text = row.link
            orphanChip.isVisible = row.isOrphan
            bindStatusChips(row.status, statusChip, statusWarningChip) { listener.onStatusClick(row) }
            chipGroup.isVisible = chipGroup.children.any { it.isVisible }
            root.setOnClickListener { listener.onRowClick(row) }
            launchButton.setOnClickListener { listener.onLaunchClick(row, it) }
            moreButton.setOnClickListener { listener.onMoreClick(row, it) }
        }
    }
}
