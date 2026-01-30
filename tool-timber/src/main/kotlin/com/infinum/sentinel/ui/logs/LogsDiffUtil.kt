package com.infinum.sentinel.ui.logs

import androidx.recyclerview.widget.DiffUtil
import java.io.File

internal class LogsDiffUtil : DiffUtil.ItemCallback<File>() {
    override fun areItemsTheSame(
        oldItem: File,
        newItem: File,
    ): Boolean = oldItem.name == newItem.name

    override fun areContentsTheSame(
        oldItem: File,
        newItem: File,
    ): Boolean = oldItem == newItem
}
