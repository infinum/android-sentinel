package com.infinum.sentinel.ui.deeplinks.editor

/**
 * What the variant editor screen reports. The activity forwards every edit to [VariantEditorViewModel], which
 * keeps the draft; the screen keeps only the text of its fields.
 */
internal data class VariantEditorCallbacks(
    val onNameChanged: (String) -> Unit,
    val onHostChanged: (String) -> Unit,
    val onPathChanged: (String) -> Unit,
    val onKeyChanged: (rowId: Long, value: String) -> Unit,
    val onValueChanged: (rowId: Long, value: String) -> Unit,
    val onAddRow: () -> Unit,
    val onRemoveRow: (rowId: Long) -> Unit,
    val onSave: () -> Unit,
    val onClose: () -> Unit,
)
