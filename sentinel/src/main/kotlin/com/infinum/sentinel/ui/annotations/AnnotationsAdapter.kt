package com.infinum.sentinel.ui.annotations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.infinum.sentinel.R
import com.infinum.sentinel.annotations.SentinelAnnotation
import com.infinum.sentinel.databinding.SentinelItemAnnotationBinding

/**
 * A list row: the note plus the number its badge shows on screen.
 *
 * The number is part of the row so that deleting a note rebinds the rows after it. Otherwise
 * they would keep their old numbers while the on-screen badges renumber.
 */
internal data class AnnotationRow(
    val number: Int,
    val annotation: SentinelAnnotation,
)

internal class AnnotationsAdapter(
    private val onDelete: (SentinelAnnotation) -> Unit,
) : ListAdapter<AnnotationRow, AnnotationsAdapter.ViewHolder>(DIFF) {
    private companion object {
        private const val SENT_ALPHA = 0.6f
        private const val ACTIVE_ALPHA = 1.0f

        private val DIFF =
            object : DiffUtil.ItemCallback<AnnotationRow>() {
                override fun areItemsTheSame(oldItem: AnnotationRow, newItem: AnnotationRow): Boolean =
                    oldItem.annotation.id == newItem.annotation.id

                override fun areContentsTheSame(oldItem: AnnotationRow, newItem: AnnotationRow): Boolean = oldItem == newItem
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            SentinelItemAnnotationBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onDelete,
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    internal class ViewHolder(
        private val binding: SentinelItemAnnotationBinding,
        private val onDelete: (SentinelAnnotation) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: AnnotationRow) {
            val annotation = row.annotation
            with(binding) {
                root.alpha = if (annotation.isSent) SENT_ALPHA else ACTIVE_ALPHA
                noteView.text = root.context.getString(R.string.sentinel_annotations_row, row.number, annotation.note)
                targetView.text = annotation.targetType
                // Views identify by resource id, composables by source location. Show whichever resolved.
                val locator = annotation.sourceLocation ?: annotation.viewId
                locationView.text = locator
                locationView.visibility = if (locator == null) View.GONE else View.VISIBLE
                sentView.visibility = if (annotation.isSent) View.VISIBLE else View.GONE
                deleteButton.setOnClickListener { confirmDelete(row) }
            }
        }

        /** Confirms first, so a stray tap cannot silently drop a note. */
        private fun confirmDelete(row: AnnotationRow) {
            MaterialAlertDialogBuilder(binding.root.context)
                .setTitle(R.string.sentinel_annotations_delete_title)
                .setMessage(binding.root.context.getString(R.string.sentinel_annotations_delete_message, row.number))
                .setNegativeButton(R.string.sentinel_cancel, null)
                .setPositiveButton(R.string.sentinel_delete) { _, _ -> onDelete(row.annotation) }
                .show()
        }
    }
}
