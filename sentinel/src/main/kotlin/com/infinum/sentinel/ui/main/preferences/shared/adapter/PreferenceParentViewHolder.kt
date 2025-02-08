package com.infinum.sentinel.ui.main.preferences.shared.adapter

import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelViewItemPreferenceBinding
import com.infinum.sentinel.ui.main.preferences.shared.model.PreferencesItem

internal class PreferenceParentViewHolder(private val binding: SentinelViewItemPreferenceBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        data: PreferencesItem.Parent,
        onSortClicked: (String) -> Unit,
        onHideExpandClicked: (String) -> Unit,
    ) {
        binding.nameView.text = data.name
        binding.sortImageView.setOnClickListener { onSortClicked(data.name) }
        binding.hideExpandImageView.setOnClickListener { onHideExpandClicked(data.name) }

        binding.hideExpandImageView.setImageResource(
            if (data.isExpanded) R.drawable.sentinel_ic_minus else R.drawable.sentinel_ic_plus
        )
    }
}
