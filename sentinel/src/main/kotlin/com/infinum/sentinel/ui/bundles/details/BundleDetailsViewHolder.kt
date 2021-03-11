package com.infinum.sentinel.ui.bundles.details

import android.text.format.Formatter
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.data.models.memory.bundles.BundleTree
import com.infinum.sentinel.databinding.SentinelItemBundleKeyBinding

internal class BundleDetailsViewHolder(
    private val binding: SentinelItemBundleKeyBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: BundleTree?, parentSize: Int) =
        item?.let { tree ->
            with(binding) {
                nameView.text = tree.id
                sizeView.text = Formatter.formatFileSize(sizeView.context, tree.size.toLong())
                magnitudeView.progress = tree.size
                magnitudeView.max = parentSize
            }
        } ?: unbind()

    fun unbind() =
        with(binding) {
            nameView.text = null
            sizeView.text = null
            magnitudeView.progress = 0
            magnitudeView.max = 0
        }
}
