package com.infinum.sentinel.ui.certificates

import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.databinding.SentinelItemHeaderBinding

internal class HeaderViewHolder(
    private val binding: SentinelItemHeaderBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        @StringRes title: Int,
        count: Int,
    ) {
        with(binding) {
            titleView.text = titleView.context.getString(title)
            countView.text = count.toString()
            countView.isVisible = count > 0
        }
    }

    fun unbind() {
        with(binding) {
            titleView.text = null
            countView.text = null
            countView.isVisible = false
        }
    }
}
