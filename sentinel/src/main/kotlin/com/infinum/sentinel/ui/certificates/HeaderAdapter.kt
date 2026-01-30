package com.infinum.sentinel.ui.certificates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.databinding.SentinelItemHeaderBinding

internal class HeaderAdapter(
    @StringRes private val title: Int,
    private var count: Int,
) : RecyclerView.Adapter<HeaderViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HeaderViewHolder =
        HeaderViewHolder(
            SentinelItemHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: HeaderViewHolder,
        position: Int,
    ) = holder.bind(title, count)

    override fun onViewRecycled(holder: HeaderViewHolder) {
        holder.unbind()
    }

    override fun getItemCount(): Int = 1

    fun updateCount(count: Int) {
        this.count = count
        if (itemCount > 0) {
            notifyItemChanged(0)
        }
    }
}
