package com.infinum.sentinel.ui.logs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.infinum.sentinel.databinding.SentinelItemLogFileBinding
import com.infinum.sentinel.ui.shared.TimberToolConstants.LOG_DATE_TIME_FORMAT
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

internal class LogsAdapter(
    private val onListChanged: (Boolean) -> Unit,
    private val onDelete: (File) -> Unit,
    private val onShare: (File) -> Unit,
) : ListAdapter<File, LogsViewHolder>(LogsDiffUtil()) {
    private val dateTimeFormat = SimpleDateFormat(LOG_DATE_TIME_FORMAT, Locale.getDefault())

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LogsViewHolder =
        LogsViewHolder(
            SentinelItemLogFileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: LogsViewHolder,
        position: Int,
    ) {
        holder.bind(
            item = getItem(position),
            dateTimeFormat = dateTimeFormat,
            onDelete = onDelete,
            onShare = onShare,
        )
    }

    override fun onViewRecycled(holder: LogsViewHolder) {
        holder.unbind()
    }

    override fun onCurrentListChanged(
        previousList: MutableList<File>,
        currentList: MutableList<File>,
    ) = onListChanged(currentList.isEmpty())
}
