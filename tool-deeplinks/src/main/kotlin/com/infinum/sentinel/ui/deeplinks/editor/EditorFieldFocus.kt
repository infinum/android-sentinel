package com.infinum.sentinel.ui.deeplinks.editor

/**
 * How an editor field takes part in focus handling: it registers its requester in [requesters] and reports
 * through [onFocused] when it gains focus, so the screen can restore that focus after recreation.
 */
internal data class EditorFieldFocus(
    val requesters: VariantEditorFocusRequesters,
    val onFocused: (fieldId: String) -> Unit,
)
