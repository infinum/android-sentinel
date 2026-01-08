package com.infinum.sentinel.ui.main.preferences.editor

import com.infinum.sentinel.data.models.raw.PreferenceType

internal sealed class PreferenceEditorState {
    data class Cache(
        val name: String,
        val type: PreferenceType,
        val key: String,
        val value: Any,
    ) : PreferenceEditorState()
}
