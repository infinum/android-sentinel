package com.infinum.sentinel.ui.logger

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.R
import com.infinum.sentinel.SentinelFileTree
import com.infinum.sentinel.databinding.SentinelItemLogBinding
import com.infinum.sentinel.ui.logger.models.Level
import java.text.SimpleDateFormat
import java.util.Date

internal class LoggerViewHolder(
    private val binding: SentinelItemLogBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: SentinelFileTree.Entry?,
        dateTimeFormat: SimpleDateFormat,
        onClick: (SentinelFileTree.Entry) -> Unit
    ) {
        item?.let { entry ->
            with(binding) {
                levelView.setBackgroundColor(
                    ContextCompat.getColor(
                        levelView.context,
                        when (entry.level) {
                            Level.ASSERT -> R.color.sentinel_log_level_assert
                            Level.DEBUG -> R.color.sentinel_log_level_debug
                            Level.ERROR -> R.color.sentinel_log_level_error
                            Level.INFO -> R.color.sentinel_log_level_info
                            Level.VERBOSE -> R.color.sentinel_log_level_verbose
                            Level.WARN -> R.color.sentinel_log_level_warn
                            Level.UNKNOWN -> R.color.sentinel_log_level_unknown
                        }
                    )
                )
                timestampView.text = dateTimeFormat.format(Date(entry.timestamp))
                tagView.text = entry.tag
                entry.stackTrace?.let {
                    stackTraceView.text = it
                    stackTraceContainer.isVisible = true
                    messageView.isVisible = false
                } ?: run {
                    messageView.text = entry.message
                    messageView.isVisible = true
                    stackTraceContainer.isVisible = false
                }
                root.setOnClickListener { onClick(entry) }
            }
        } ?: unbind()
    }

    fun unbind() =
        with(binding) {
            levelView.background = null
            timestampView.text = null
            tagView.text = null
            messageView.text = null
            messageView.isVisible = false
            stackTraceView.text = null
            stackTraceContainer.isVisible = false
            root.setOnClickListener(null)
        }
}
