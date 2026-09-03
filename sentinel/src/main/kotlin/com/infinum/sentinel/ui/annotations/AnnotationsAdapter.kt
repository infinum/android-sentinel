package com.infinum.sentinel.ui.annotations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.databinding.SentinelItemAnnotationBinding

internal class AnnotationsAdapter(
    private val onDelete: (Annotation) -> Unit,
) : ListAdapter<Annotation, AnnotationsAdapter.ViewHolder>(DIFF) {
    private companion object {
        private const val SENT_ALPHA = 0.6f
        private const val ACTIVE_ALPHA = 1.0f

        private val DIFF =
            object : DiffUtil.ItemCallback<Annotation>() {
                override fun areItemsTheSame(oldItem: Annotation, newItem: Annotation): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: Annotation, newItem: Annotation): Boolean = oldItem == newItem
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            SentinelItemAnnotationBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onDelete,
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position), position)

    internal class ViewHolder(
        private val binding: SentinelItemAnnotationBinding,
        private val onDelete: (Annotation) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(annotation: Annotation, position: Int) {
            with(binding) {
                root.alpha = if (annotation.isSent) SENT_ALPHA else ACTIVE_ALPHA
                noteView.text = root.context.getString(R.string.sentinel_annotations_row, position + 1, annotation.note)
                targetView.text = annotation.targetType
                // Views identify by resource id, composables by source location. Show whichever resolved.
                val locator = annotation.sourceLocation ?: annotation.viewId
                locationView.text = locator
                locationView.visibility = if (locator == null) View.GONE else View.VISIBLE
                sentView.visibility = if (annotation.isSent) View.VISIBLE else View.GONE
                deleteButton.setOnClickListener { confirmDelete(annotation, position) }
            }
        }

        /** Confirms first, so a stray tap cannot silently drop a note. */
        private fun confirmDelete(annotation: Annotation, position: Int) {
            MaterialAlertDialogBuilder(binding.root.context)
                .setTitle(R.string.sentinel_annotations_delete_title)
                .setMessage(binding.root.context.getString(R.string.sentinel_annotations_delete_message, position + 1))
                .setNegativeButton(R.string.sentinel_cancel, null)
                .setPositiveButton(R.string.sentinel_delete) { _, _ -> onDelete(annotation) }
                .show()
        }
    }
}
