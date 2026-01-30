package com.infinum.sentinel.ui.bundles

import android.text.format.Formatter
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.databinding.SentinelItemBundleBinding
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

internal class BundleViewHolder(
    private val binding: SentinelItemBundleBinding,
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        private const val BYTES_DIVIDER = 1000.0f
    }

    fun bind(
        item: BundleDescriptor?,
        onClick: (BundleDescriptor) -> Unit,
    ) = item?.let { descriptor ->
        with(binding) {
            iconView.setImageResource(descriptor.callSite.icon)
            timestampView.text = SimpleDateFormat.getTimeInstance().format(Date(descriptor.timestamp))
            typeView.text = typeView.context.getString(descriptor.callSite.text)
            callClassNameView.text = descriptor.className
            sizeView.text = Formatter.formatFileSize(sizeView.context, descriptor.bundleTree.size.toLong())
            magnitudeView.progress = (descriptor.bundleTree.size / BYTES_DIVIDER).roundToInt()
            magnitudeView.secondaryProgress = descriptor.limit
            root.setOnClickListener { onClick(descriptor) }
        }
    } ?: unbind()

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
