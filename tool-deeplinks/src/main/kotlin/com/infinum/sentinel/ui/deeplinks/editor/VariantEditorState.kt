package com.infinum.sentinel.ui.deeplinks.editor

import com.infinum.sentinel.ui.deeplinks.launch.LaunchRequest

internal sealed class VariantEditorState {
    /**
     * The editor with its fixed [form], the current [draft], its [validation] and the final [previewLink].
     * [isDirty] is true when the draft differs from the one the editor started with.
     * Errors are shown only when [isShowingErrors] is true, which happens after a save attempt.
     */
    data class Data(
        val form: VariantEditorForm,
        val draft: VariantDraft,
        val validation: VariantDraftValidation,
        val previewLink: String,
        val isDirty: Boolean,
        val isShowingErrors: Boolean,
    ) : VariantEditorState()

    /** The base link or variant isn't available anymore. */
    data object Missing : VariantEditorState()

    /** The variant was saved and the screen should close, then run [launch] when the tester chose Launch. */
    data class Saved(
        val launch: LaunchRequest?,
    ) : VariantEditorState()
}
