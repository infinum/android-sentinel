package com.infinum.sentinel.ui.crash

import androidx.recyclerview.widget.DiffUtil
import com.infinum.sentinel.data.models.local.CrashEntity

internal class CrashesDiffUtil : DiffUtil.ItemCallback<CrashEntity>() {
    override fun areItemsTheSame(
        oldItem: CrashEntity,
        newItem: CrashEntity,
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: CrashEntity,
        newItem: CrashEntity,
    ): Boolean = oldItem == newItem
}
