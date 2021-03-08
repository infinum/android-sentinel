package com.infinum.sentinel.ui.bundles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.databinding.SentinelItemBundleBinding
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor

internal class BundlesAdapter(
    private val onClick: (BundleDescriptor) -> Unit
) : RecyclerView.Adapter<BundleViewHolder>() {

    private var items = mutableListOf<BundleDescriptor>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BundleViewHolder =
        BundleViewHolder(
            SentinelItemBundleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: BundleViewHolder, position: Int) =
        holder.bind(items[position], onClick)

    override fun onViewRecycled(holder: BundleViewHolder) {
        holder.unbind()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int =
        items.size

    fun add(item: BundleDescriptor) {
        items.add(item)
        notifyDataSetChanged()
    }
}
