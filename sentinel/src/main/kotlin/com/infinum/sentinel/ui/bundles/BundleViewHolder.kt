package com.infinum.sentinel.ui.bundles

import android.text.format.Formatter
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.databinding.SentinelItemBundleBinding
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

internal class BundleViewHolder(
    private val binding: SentinelItemBundleBinding
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val BYTES_DIVIDER = 1000.0f
    }

    fun bind(item: BundleDescriptor, onClick: (BundleDescriptor) -> Unit) =
        with(binding) {
            iconView.setImageResource(item.callSite.icon)
            timestampView.text = SimpleDateFormat.getTimeInstance().format(Date(item.timestamp))
            typeView.text = typeView.context.getString(item.callSite.text)
            callClassNameView.text = item.className
            sizeView.text = Formatter.formatFileSize(sizeView.context, item.bundleTree.size.toLong())
            magnitudeView.progress = (item.bundleTree.size / BYTES_DIVIDER).roundToInt()
            magnitudeView.secondaryProgress = item.limit
            root.setOnClickListener { onClick(item) }
        }

    fun unbind() =
        with(binding) {
            iconView.setImageDrawable(null)
            timestampView.text = null
            typeView.text = null
            callClassNameView.text = null
            sizeView.text = null
            magnitudeView.progress = 0
            magnitudeView.secondaryProgress = 0
            root.setOnClickListener(null)
        }
}
