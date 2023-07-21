package com.infinum.sentinel.ui.logs

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.R
import com.infinum.sentinel.SentinelFileTree
import com.infinum.sentinel.databinding.SentinelItemLogFileBinding
import com.infinum.sentinel.ui.logger.models.Level
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

internal class LogsViewHolder(
    private val binding: SentinelItemLogFileBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: File?, onDelete: (File) -> Unit, onShare: (File) -> Unit) {
        item?.let { entry ->
            with(binding) {
                messageView.text = entry.name
                timestampView.text = SimpleDateFormat.getDateTimeInstance().format(Date(entry.lastModified()))
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