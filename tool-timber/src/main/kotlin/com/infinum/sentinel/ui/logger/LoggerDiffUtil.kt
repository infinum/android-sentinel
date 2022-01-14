package com.infinum.sentinel.ui.logger

import androidx.recyclerview.widget.DiffUtil
import com.infinum.sentinel.SentinelTree

internal class LoggerDiffUtil : DiffUtil.ItemCallback<SentinelTree.Entry>() {

    override fun areItemsTheSame(oldItem: SentinelTree.Entry, newItem: SentinelTree.Entry): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: SentinelTree.Entry, newItem: SentinelTree.Entry): Boolean {
        return oldItem == newItem
    }
}
