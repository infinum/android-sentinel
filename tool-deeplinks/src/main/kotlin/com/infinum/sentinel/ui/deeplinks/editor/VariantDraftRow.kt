package com.infinum.sentinel.ui.deeplinks.editor

import kotlinx.serialization.Serializable

/**
 * One query parameter row of the editor. [key] and [value] are plain text, kept exactly as typed.
 * A row with neither a key nor a value is blank: it isn't checked and isn't part of the link.
 *
 * @property rowId identity of the row inside its draft. It never changes and is never reused.
 * @property key the parameter key as typed.
 * @property value the parameter value as typed.
 */
@Serializable
internal data class VariantDraftRow(
    val rowId: Long,
    val key: String,
    val value: String,
) {
    val isBlank: Boolean
        get() = key.isBlank() && value.isBlank()
}
