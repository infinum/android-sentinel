package com.infinum.sentinel.ui.deeplinks.list

import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelItemDeepLinkNoticeBinding
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksNotice

/**
 * Shows a notice above the sections. A new [DeepLinksNotice] entry doesn't compile until both mappings handle it.
 */
internal class NoticeViewHolder(
    private val binding: SentinelItemDeepLinkNoticeBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        item: DeepLinksItem.Notice,
        listener: DeepLinksListener,
    ) {
        val action = actionOf(item.notice)
        with(binding) {
            messageView.setText(messageOf(item.notice))
            actionButton.isVisible = action != null
            action?.let { actionButton.setText(it) }
            actionButton.setOnClickListener { listener.onNoticeActionClick(item.notice) }
        }
    }

    @StringRes
    private fun messageOf(notice: DeepLinksNotice): Int =
        when (notice) {
            DeepLinksNotice.LINK_HANDLING_OFF -> R.string.sentinel_deep_links_notice_link_handling_off
            DeepLinksNotice.DISCOVERY_FAILED -> R.string.sentinel_deep_links_discovery_failed
        }

    @StringRes
    private fun actionOf(notice: DeepLinksNotice): Int? =
        when (notice) {
            DeepLinksNotice.LINK_HANDLING_OFF -> R.string.sentinel_deep_links_menu_open_link_settings
            DeepLinksNotice.DISCOVERY_FAILED -> null
        }
}
