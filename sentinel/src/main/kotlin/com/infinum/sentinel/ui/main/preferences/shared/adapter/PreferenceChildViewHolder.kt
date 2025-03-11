package com.infinum.sentinel.ui.main.preferences.shared.adapter

import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.databinding.SentinelViewItemTextBinding
import com.infinum.sentinel.extensions.copyToClipboard
import com.infinum.sentinel.ui.main.preferences.shared.model.PreferencesItem

internal class PreferenceChildViewHolder(private val binding: SentinelViewItemTextBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        data: PreferencesItem.Child,
        onPreferenceClicked: ((String, Triple<PreferenceType, String, Any>) -> Unit)?,
    ) {
        binding.labelView.text = data.label
        binding.valueView.text = data.value.toString()

        binding.root.setOnClickListener {
            onPreferenceClicked?.invoke(data.parentName, Triple(data.preferenceType, data.label, data.value))
        }

        binding.root.setOnLongClickListener {
            it.context.copyToClipboard(key = data.label, value = data.value.toString())
        }
    }
}
