package com.infinum.sentinel.ui.deeplinks.editor

import kotlinx.serialization.Serializable

/**
 * The editor state kept across process death: the draft as first created and as it is now.
 */
@Serializable
internal data class VariantEditorSavedState(
    val initial: VariantDraft,
    val current: VariantDraft,
)
