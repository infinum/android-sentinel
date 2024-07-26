package com.infinum.sentinel.ui.logs

import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.databinding.SentinelItemLogFileBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

internal class LogsViewHolder(
    private val binding: SentinelItemLogFileBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: File?, dateTimeFormat: SimpleDateFormat, onDelete: (File) -> Unit, onShare: (File) -> Unit) {
        item?.let { entry ->
            with(binding) {
                messageView.text = entry.name
                timestampView.text = dateTimeFormat.format(Date(entry.lastModified()))
                deleteButton.setOnClickListener { onDelete(entry) }
                shareButton.setOnClickListener { onShare(entry) }
            }
        } ?: unbind()
    }

    fun unbind() =
        with(binding) {
            timestampView.text = null
            messageView.text = null
            deleteButton.setOnClickListener(null)
            shareButton.setOnClickListener(null)
        }
}
