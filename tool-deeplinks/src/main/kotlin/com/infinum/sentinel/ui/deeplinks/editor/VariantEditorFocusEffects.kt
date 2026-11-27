package com.infinum.sentinel.ui.deeplinks.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * The id of the field to fix first after a refused save: the host, then the path, then the key of the first row
 * whose key is missing. Null when nothing is invalid.
 */
internal fun firstInvalidFieldId(state: VariantEditorState.Data): String? {
    val validation = state.validation
    return when {
        validation.isHostMissing -> VariantEditorFocusRequesters.HOST
        validation.isPathMissing -> VariantEditorFocusRequesters.PATH
        else -> state.draft.rows.firstOrNull { it.rowId in validation.emptyKeyRowIds }?.let { VariantEditorFocusRequesters.key(it.rowId) }
    }
}

/**
 * Gives focus back to the field that had it before the activity was recreated, as an XML form restores the
 * focused view. [focusedFieldId] is the saved id from the first composition, null on a fresh screen.
 */
@Composable
internal fun RestoreFocusEffect(
    focusedFieldId: String?,
    focusRequesters: VariantEditorFocusRequesters,
) {
    LaunchedEffect(Unit) {
        if (focusedFieldId != null) {
            withFrameNanos { }
            focusRequesters.find(focusedFieldId)?.requestFocus()
        }
    }
}

/**
 * Focuses the key field of the row [rowId] once that row is in [data], then calls [onFocused]. A row added by
 * "Add parameter" in the editor appears in the next state, after the event that asks to focus it.
 */
@Composable
internal fun PendingRowFocusEffect(
    rowId: Long?,
    data: VariantEditorState.Data,
    focusRequesters: VariantEditorFocusRequesters,
    onFocused: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val currentOnFocused by rememberUpdatedState(onFocused)
    val isRowShown = rowId != null && data.draft.rows.any { it.rowId == rowId }
    LaunchedEffect(rowId, isRowShown) {
        if (rowId != null && isRowShown) {
            withFrameNanos { }
            focusRequesters.find(VariantEditorFocusRequesters.key(rowId))?.requestFocus()
            keyboard?.show()
            currentOnFocused()
        }
    }
}
