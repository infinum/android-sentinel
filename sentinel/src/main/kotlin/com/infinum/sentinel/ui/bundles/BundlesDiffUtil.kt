package com.infinum.sentinel.ui.bundles

import androidx.recyclerview.widget.DiffUtil
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor

internal class BundlesDiffUtil : DiffUtil.ItemCallback<BundleDescriptor>() {
    override fun areItemsTheSame(
        oldItem: BundleDescriptor,
        newItem: BundleDescriptor,
    ): Boolean = oldItem.bundleTree.id == newItem.bundleTree.id

    override fun areContentsTheSame(
        oldItem: BundleDescriptor,
        newItem: BundleDescriptor,
    ): Boolean = oldItem == newItem
}
