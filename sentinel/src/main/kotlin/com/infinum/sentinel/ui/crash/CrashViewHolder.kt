package com.infinum.sentinel.ui.crash

import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.data.models.local.CrashEntity
import com.infinum.sentinel.databinding.SentinelItemCrashBinding
import java.text.SimpleDateFormat
import java.util.Date

internal class CrashViewHolder(
    private val binding: SentinelItemCrashBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: CrashEntity?, onClick: (CrashEntity) -> Unit) =
        item?.let { descriptor ->
            with(binding) {
                lineView.text = listOfNotNull(
                    descriptor.data.exception?.file,
                    descriptor.data.exception?.lineNumber
                ).joinToString(":")
                timestampView.text = SimpleDateFormat.getTimeInstance().format(Date(descriptor.timestamp))
                exceptionView.text = descriptor.data.exception?.name
                root.setOnClickListener { onClick(descriptor) }
            }
        } ?: unbind()

    fun unbind() =
        with(binding) {
            iconView.setImageDrawable(null)
            lineView.text = null
            timestampView.text = null
            exceptionView.text = null
            root.setOnClickListener(null)
        }
}
