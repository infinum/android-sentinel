package com.infinum.sentinel.ui.annotations.overlay

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelDialogAnnotationNoteBinding

/**
 * The note input sheet.
 *
 * Built against a [ContextThemeWrapper] carrying Sentinel's own theme rather than the host
 * Activity's: the host theme is arbitrary and may not supply the Material attributes
 * [BottomSheetDialog] needs.
 *
 * While the text differs from what the sheet opened with, a tap outside, back or a swipe does not
 * close it, so a typed note is only discarded through Cancel.
 */
internal object AnnotationNoteDialog {
    /** An open sheet. [draft] reads the current text so it can survive the host being recreated. */
    class Sheet internal constructor(
        private val dialog: BottomSheetDialog,
        private val binding: SentinelDialogAnnotationNoteBinding,
    ) {
        val draft: String get() = binding.sentinelNoteInput.text?.toString().orEmpty()

        /** Closes the sheet without calling any of its callbacks. */
        fun dismissSilently() {
            dialog.setOnDismissListener(null)
            dialog.dismiss()
        }
    }

    @Suppress("LongParameterList")
    fun show(
        context: Context,
        targetType: String,
        initial: String?,
        draft: String?,
        onSave: (String) -> Unit,
        onDelete: () -> Unit,
        onDismiss: () -> Unit,
    ): Sheet {
        val themed = ContextThemeWrapper(context, R.style.Sentinel_Theme)
        val binding = SentinelDialogAnnotationNoteBinding.inflate(LayoutInflater.from(themed))
        val dialog =
            BottomSheetDialog(themed).apply {
                setContentView(binding.root)
                // Fully open, so the input and buttons are visible in landscape too.
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

        val editing = initial != null
        binding.sentinelTitleView.text =
            themed.getString(
                if (editing) R.string.sentinel_annotations_note_edit else R.string.sentinel_annotations_note_new,
                targetType,
            )
        val opening = initial.orEmpty()
        binding.sentinelNoteInput.setText(draft ?: opening)
        binding.sentinelDeleteButton.visibility = if (editing) View.VISIBLE else View.GONE

        fun protectDraft(text: String) = dialog.setCancelable(text == opening)
        protectDraft(draft ?: opening)
        binding.sentinelNoteInput.doAfterTextChanged { protectDraft(it?.toString().orEmpty()) }

        binding.sentinelDeleteButton.setOnClickListener {
            onDelete()
            dialog.dismiss()
        }
        binding.sentinelCancelButton.setOnClickListener { dialog.dismiss() }
        binding.sentinelSaveButton.setOnClickListener {
            val note = binding.sentinelNoteInput.text?.toString()?.trim().orEmpty()
            // Emptying an existing note and saving removes it.
            if (note.isEmpty() && editing) onDelete() else if (note.isNotEmpty()) onSave(note)
            dialog.dismiss()
        }
        dialog.setOnDismissListener { onDismiss() }

        dialog.show()
        return Sheet(dialog, binding)
    }
}
