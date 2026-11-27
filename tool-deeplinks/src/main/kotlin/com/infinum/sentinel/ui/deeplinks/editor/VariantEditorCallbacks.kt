package com.infinum.sentinel.ui.deeplinks.editor

import com.infinum.sentinel.domain.deeplinks.models.LaunchMode
import com.infinum.sentinel.ui.deeplinks.launch.LaunchRequest

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
    /** Saves, then launches the link in the given mode once the editor closed. */
    val onSaveAndLaunch: (LaunchMode) -> Unit,
    /** Closes the editor; [LaunchRequest] is set when it saved with Launch. */
    val onClose: (LaunchRequest?) -> Unit,
)
