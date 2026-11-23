package com.infinum.sentinel.ui.deeplinks.list

import androidx.recyclerview.widget.DiffUtil

/**
 * Item identity for the Deep links list. A base link is identified by its origin and key, because one key can
 * be declared by two screens.
 */
internal class DeepLinksDiffUtil : DiffUtil.ItemCallback<DeepLinksItem>() {
    override fun areItemsTheSame(
        oldItem: DeepLinksItem,
        newItem: DeepLinksItem,
    ): Boolean =
        when (oldItem) {
            is DeepLinksItem.Notice -> newItem is DeepLinksItem.Notice && oldItem.notice == newItem.notice
            is DeepLinksItem.SectionHeader -> newItem is DeepLinksItem.SectionHeader && oldItem.titleRes == newItem.titleRes
            is DeepLinksItem.ScreenHeader -> newItem is DeepLinksItem.ScreenHeader && oldItem.componentName == newItem.componentName
            is DeepLinksItem.BaseLinkRow ->
                newItem is DeepLinksItem.BaseLinkRow &&
                    oldItem.baseLink.origin == newItem.baseLink.origin &&
                    oldItem.baseLink.key == newItem.baseLink.key
            is DeepLinksItem.VariantRow -> newItem is DeepLinksItem.VariantRow && oldItem.variant.id == newItem.variant.id
        }

    override fun areContentsTheSame(
        oldItem: DeepLinksItem,
        newItem: DeepLinksItem,
    ): Boolean = oldItem == newItem
}
