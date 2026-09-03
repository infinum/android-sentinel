package com.infinum.sentinel.ui.annotations.overlay

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelDialogAnnotationNoteBinding

/**
 * The note input sheet.
 *
 * Built against a [ContextThemeWrapper] carrying Sentinel's own theme rather than the host
 * Activity's: the host theme is arbitrary and may not supply the Material attributes
 * [BottomSheetDialog] needs.
 */
internal object AnnotationNoteDialog {
    fun show(
        context: Context,
        targetType: String,
        initial: String?,
        onSave: (String) -> Unit,
        onDelete: () -> Unit,
    ) {
        val themed = ContextThemeWrapper(context, R.style.Sentinel_Theme)
        val binding = SentinelDialogAnnotationNoteBinding.inflate(LayoutInflater.from(themed))
        val dialog = BottomSheetDialog(themed).apply { setContentView(binding.root) }

        val editing = initial != null
        binding.sentinelTitleView.text =
            themed.getString(
                if (editing) R.string.sentinel_annotations_note_edit else R.string.sentinel_annotations_note_new,
                targetType,
            )
        binding.sentinelNoteInput.setText(initial.orEmpty())
        binding.sentinelDeleteButton.visibility = if (editing) View.VISIBLE else View.GONE

        binding.sentinelDeleteButton.setOnClickListener {
            onDelete()
            dialog.dismiss()
        }
        binding.sentinelCancelButton.setOnClickListener { dialog.dismiss() }
        binding.sentinelSaveButton.setOnClickListener {
            val note = binding.sentinelNoteInput.text?.toString()?.trim().orEmpty()
            // An emptied note on an existing annotation means remove it, matching the sheet in borealis.
            if (note.isEmpty() && editing) onDelete() else if (note.isNotEmpty()) onSave(note)
            dialog.dismiss()
        }

        dialog.show()
    }
}
