package com.infinum.sentinel.ui.main.preferences.shared.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.databinding.SentinelViewItemPreferenceBinding
import com.infinum.sentinel.databinding.SentinelViewItemTextBinding
import com.infinum.sentinel.ui.main.preferences.shared.model.PreferencesItem

internal class PreferencesAdapter(
    private val onSortClicked: (String) -> Unit = {},
    private val onHideExpandClick: (String) -> Unit = {},
    private val onPreferenceClicked: ((String, Triple<PreferenceType, String, Any>) -> Unit)? = null
) : ListAdapter<PreferencesItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val PARENT_TYPE = 1
        private const val CHILD_TYPE = 2

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PreferencesItem>() {
            override fun areItemsTheSame(oldItem: PreferencesItem, newItem: PreferencesItem): Boolean {
                return when {
                    oldItem is PreferencesItem.Parent && newItem is PreferencesItem.Parent -> oldItem.name == newItem.name
                    oldItem is PreferencesItem.Child && newItem is PreferencesItem.Child -> oldItem.label == newItem.label
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: PreferencesItem, newItem: PreferencesItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PreferencesItem.Parent -> PARENT_TYPE
            is PreferencesItem.Child -> CHILD_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == PARENT_TYPE) {
            val binding = SentinelViewItemPreferenceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            PreferenceParentViewHolder(binding)
        } else {
            val binding = SentinelViewItemTextBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            PreferenceChildViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PreferencesItem.Parent -> (holder as PreferenceParentViewHolder).bind(
                data = item,
                onSortClicked = onSortClicked,
                onHideExpandClicked = onHideExpandClick
            )

            is PreferencesItem.Child -> (holder as PreferenceChildViewHolder).bind(
                data = item,
                onPreferenceClicked = onPreferenceClicked
            )
        }
    }
}
