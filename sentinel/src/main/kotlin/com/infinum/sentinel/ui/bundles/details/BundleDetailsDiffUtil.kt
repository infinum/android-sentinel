package com.infinum.sentinel.ui.bundles.details

import androidx.recyclerview.widget.DiffUtil
import com.infinum.sentinel.data.models.memory.bundles.BundleTree

internal class BundleDetailsDiffUtil : DiffUtil.ItemCallback<BundleTree>() {
    override fun areItemsTheSame(
        oldItem: BundleTree,
        newItem: BundleTree,
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: BundleTree,
        newItem: BundleTree,
    ): Boolean = oldItem == newItem
}
