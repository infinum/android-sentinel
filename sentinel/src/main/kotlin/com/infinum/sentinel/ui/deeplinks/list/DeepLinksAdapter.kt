package com.infinum.sentinel.ui.deeplinks.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.databinding.SentinelItemDeepLinkBinding
import com.infinum.sentinel.databinding.SentinelItemDeepLinkNoticeBinding
import com.infinum.sentinel.databinding.SentinelItemDeepLinkVariantBinding
import com.infinum.sentinel.databinding.SentinelItemHeaderBinding

/**
 * The Deep links list: notices, section headers, base links and variants. Clicks go to the listener.
 */
internal class DeepLinksAdapter(
    private val listener: DeepLinksListener,
) : ListAdapter<DeepLinksItem, RecyclerView.ViewHolder>(DeepLinksDiffUtil()) {
    companion object {
        private const val NOTICE_TYPE = 1
        private const val HEADER_TYPE = 2
        private const val BASE_LINK_TYPE = 3
        private const val VARIANT_TYPE = 4
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is DeepLinksItem.Notice -> NOTICE_TYPE
            is DeepLinksItem.SectionHeader -> HEADER_TYPE
            is DeepLinksItem.ScreenHeader -> HEADER_TYPE
            is DeepLinksItem.BaseLinkRow -> BASE_LINK_TYPE
            is DeepLinksItem.VariantRow -> VARIANT_TYPE
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            NOTICE_TYPE -> NoticeViewHolder(SentinelItemDeepLinkNoticeBinding.inflate(inflater, parent, false))
            HEADER_TYPE -> DeepLinksHeaderViewHolder(SentinelItemHeaderBinding.inflate(inflater, parent, false))
            BASE_LINK_TYPE -> BaseLinkViewHolder(SentinelItemDeepLinkBinding.inflate(inflater, parent, false))
            VARIANT_TYPE -> VariantViewHolder(SentinelItemDeepLinkVariantBinding.inflate(inflater, parent, false))
            else -> error("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (val item = getItem(position)) {
            is DeepLinksItem.Notice -> (holder as NoticeViewHolder).bind(item, listener)
            is DeepLinksItem.SectionHeader -> (holder as DeepLinksHeaderViewHolder).bind(item)
            is DeepLinksItem.ScreenHeader -> (holder as DeepLinksHeaderViewHolder).bind(item)
            is DeepLinksItem.BaseLinkRow -> (holder as BaseLinkViewHolder).bind(item, listener)
            is DeepLinksItem.VariantRow -> (holder as VariantViewHolder).bind(item, listener)
        }
    }
}
