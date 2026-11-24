package com.infinum.sentinel.ui.deeplinks.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
 * The id of the field the editor focuses on its first load: the name for Rename, and for Add parameter the key of
 * the row the view model appended before its first state. Null when nothing is focused.
 */
internal fun initialFocusFieldId(
    initialFocus: VariantEditorFocus,
    state: VariantEditorState.Data,
): String? =
    when (initialFocus) {
        VariantEditorFocus.NAME -> VariantEditorFocusRequesters.NAME
        VariantEditorFocus.NEW_PARAMETER -> state.draft.rows.lastOrNull()?.let { VariantEditorFocusRequesters.key(it.rowId) }
        VariantEditorFocus.NONE -> null
    }

/**
 * Focuses the field of [initialFocusFieldId] and shows the keyboard, once per screen: the flag is saved, so neither
 * a rotation nor a restore after process death focuses it again. This reads the first state rather than waiting
 * for the view model's focus event, which is emitted before the screen subscribes.
 */
@Composable
internal fun InitialFocusEffect(
    initialFocus: VariantEditorFocus,
    data: VariantEditorState.Data,
    focusRequesters: VariantEditorFocusRequesters,
) {
    var isFocusHandled by rememberSaveable { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        val fieldId = if (isFocusHandled) null else initialFocusFieldId(initialFocus, data)
        isFocusHandled = true
        if (fieldId != null) {
            // Wait one frame, so the field is composed and has registered its focus requester.
            withFrameNanos { }
            focusRequesters.find(fieldId)?.requestFocus()
            keyboard?.show()
        }
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
