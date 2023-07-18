package com.infinum.sentinel.ui.logs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.infinum.sentinel.SentinelFileTree
import com.infinum.sentinel.databinding.SentinelItemLogBinding
import com.infinum.sentinel.databinding.SentinelItemLogFileBinding
import java.io.File

internal class LogsAdapter(
    private val onListChanged: (Boolean) -> Unit,
    private val onDelete: (File) -> Unit,
    private val onShare: (File) -> Unit
) : ListAdapter<File, LogsViewHolder>(LogsDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsViewHolder =
        LogsViewHolder(
            SentinelItemLogFileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: LogsViewHolder, position: Int) {
        holder.bind(getItem(position), onDelete, onShare)
    }

    override fun onViewRecycled(holder: LogsViewHolder) {
        holder.unbind()
    }

    override fun onCurrentListChanged(
        previousList: MutableList<File>,
        currentList: MutableList<File>
    ) =
        onListChanged(currentList.isEmpty())
}