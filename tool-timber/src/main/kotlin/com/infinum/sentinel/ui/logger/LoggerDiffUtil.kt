package com.infinum.sentinel.ui.logger

import androidx.recyclerview.widget.DiffUtil
import com.infinum.sentinel.SentinelFileTree

internal class LoggerDiffUtil : DiffUtil.ItemCallback<SentinelFileTree.Entry>() {

    override fun areItemsTheSame(
        oldItem: SentinelFileTree.Entry,
        newItem: SentinelFileTree.Entry
    ): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(
        oldItem: SentinelFileTree.Entry,
        newItem: SentinelFileTree.Entry
    ): Boolean {
        return oldItem == newItem
    }
}
