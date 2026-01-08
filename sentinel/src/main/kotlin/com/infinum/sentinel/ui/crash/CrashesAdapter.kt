package com.infinum.sentinel.ui.crash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.infinum.sentinel.data.models.local.CrashEntity
import com.infinum.sentinel.databinding.SentinelItemCrashBinding

internal class CrashesAdapter(
    private val onListChanged: (Boolean) -> Unit,
    private val onClick: (CrashEntity) -> Unit,
) : ListAdapter<CrashEntity, CrashViewHolder>(CrashesDiffUtil()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CrashViewHolder =
        CrashViewHolder(
            SentinelItemCrashBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: CrashViewHolder,
        position: Int,
    ) = holder.bind(getItem(position), onClick)

    override fun onViewRecycled(holder: CrashViewHolder) {
        holder.unbind()
    }

    override fun onCurrentListChanged(
        previousList: MutableList<CrashEntity>,
        currentList: MutableList<CrashEntity>,
    ) = onListChanged(currentList.isEmpty())
}
