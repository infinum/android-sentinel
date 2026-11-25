package com.infinum.sentinel.ui.deeplinks.editor

/**
 * One-off events of the variant editor.
 */
internal sealed class VariantEditorEvent {
    /** Focus the key field of the row with [rowId]. */
    data class FocusRow(
        val rowId: Long,
    ) : VariantEditorEvent()

    /** Saving was refused because the draft has errors. The screen shows them and focuses the first one. */
    data object ValidationFailed : VariantEditorEvent()

    /** The variant could not be saved. */
    data object SaveFailed : VariantEditorEvent()
}
