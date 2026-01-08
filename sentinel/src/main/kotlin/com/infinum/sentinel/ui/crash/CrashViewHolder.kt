package com.infinum.sentinel.ui.crash

import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.CrashEntity
import com.infinum.sentinel.databinding.SentinelItemCrashBinding
import java.text.SimpleDateFormat
import java.util.Date

internal class CrashViewHolder(
    private val binding: SentinelItemCrashBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        item: CrashEntity?,
        onClick: (CrashEntity) -> Unit,
    ) = item?.let { descriptor ->
        with(binding) {
            iconView.setImageResource(
                if (item.data.exception?.isANRException == true) {
                    R.drawable.sentinel_ic_anr
                } else {
                    R.drawable.sentinel_ic_crash
                },
            )
            lineView.text =
                if (item.data.exception?.isANRException == true) {
                    lineView.context.getString(R.string.sentinel_anr_message)
                } else {
                    listOfNotNull(
                        descriptor.data.exception?.file,
                        descriptor.data.exception?.lineNumber,
                    ).joinToString(":")
                }
            timestampView.text = SimpleDateFormat.getDateTimeInstance().format(Date(descriptor.timestamp))
            exceptionView.text =
                if (item.data.exception?.isANRException == true) {
                    lineView.context.getString(R.string.sentinel_anr_title)
                } else {
                    descriptor.data.exception?.name
                }
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
