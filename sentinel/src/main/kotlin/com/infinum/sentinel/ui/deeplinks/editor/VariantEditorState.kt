package com.infinum.sentinel.ui.deeplinks.editor

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

    /** The variant was saved and the screen should close. */
    data object Saved : VariantEditorState()
}
