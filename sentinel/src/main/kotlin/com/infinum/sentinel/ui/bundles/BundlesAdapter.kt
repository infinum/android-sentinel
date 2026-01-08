package com.infinum.sentinel.ui.bundles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.infinum.sentinel.databinding.SentinelItemBundleBinding
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor

internal class BundlesAdapter(
    private val onListChanged: (Boolean) -> Unit,
    private val onClick: (BundleDescriptor) -> Unit,
) : ListAdapter<BundleDescriptor, BundleViewHolder>(BundlesDiffUtil()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BundleViewHolder =
        BundleViewHolder(
            SentinelItemBundleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: BundleViewHolder,
        position: Int,
    ) = holder.bind(getItem(position), onClick)

    override fun onViewRecycled(holder: BundleViewHolder) {
        holder.unbind()
    }

    override fun onCurrentListChanged(
        previousList: MutableList<BundleDescriptor>,
        currentList: MutableList<BundleDescriptor>,
    ) = onListChanged(currentList.isEmpty())
}
