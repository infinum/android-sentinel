package com.infinum.sentinel.ui.logger

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.infinum.sentinel.SentinelFileTree
import com.infinum.sentinel.databinding.SentinelItemLogBinding

internal class LoggerAdapter(
    private val onListChanged: (Boolean) -> Unit,
    private val onClick: (SentinelFileTree.Entry) -> Unit
) : ListAdapter<SentinelFileTree.Entry, LoggerViewHolder>(LoggerDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoggerViewHolder =
        LoggerViewHolder(
            SentinelItemLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: LoggerViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    override fun onViewRecycled(holder: LoggerViewHolder) {
        holder.unbind()
    }

    override fun onCurrentListChanged(
        previousList: MutableList<SentinelFileTree.Entry>,
        currentList: MutableList<SentinelFileTree.Entry>
    ) =
        onListChanged(currentList.isEmpty())
}
