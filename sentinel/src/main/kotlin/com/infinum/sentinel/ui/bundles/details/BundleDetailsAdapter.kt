package com.infinum.sentinel.ui.bundles.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.infinum.sentinel.data.models.memory.bundles.BundleTree
import com.infinum.sentinel.databinding.SentinelItemBundleKeyBinding

internal class BundleDetailsAdapter(
    private val parentSize: Int
) : ListAdapter<BundleTree, BundleDetailsViewHolder>(BundleDetailsDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BundleDetailsViewHolder =
        BundleDetailsViewHolder(
            SentinelItemBundleKeyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: BundleDetailsViewHolder, position: Int) =
        holder.bind(getItem(position), parentSize)

    override fun onViewRecycled(holder: BundleDetailsViewHolder) {
        holder.unbind()
        super.onViewRecycled(holder)
    }
}
