package com.infinum.sentinel.ui.deeplinks.editor

import androidx.compose.ui.focus.FocusRequester

/**
 * The focus targets of the variant editor, by field id. Each field registers its own requester while it is in the
 * composition, so a removed row takes its requester with it and a lookup never creates one.
 */
internal class VariantEditorFocusRequesters {
    companion object {
        const val NAME: String = "name"
        const val HOST: String = "host"
        const val PATH: String = "path"

        /** The id of the key field of the row [rowId]. */
        fun key(rowId: Long): String = "key:$rowId"

        /** The id of the value field of the row [rowId]. */
        fun value(rowId: Long): String = "value:$rowId"
    }

    private val requesters = mutableMapOf<String, FocusRequester>()

    /** Called by a field when it enters the composition. */
    fun register(
        fieldId: String,
        requester: FocusRequester,
    ) {
        requesters[fieldId] = requester
    }

    /** Called by a field when it leaves the composition. */
    fun unregister(
        fieldId: String,
        requester: FocusRequester,
    ) {
        if (requesters[fieldId] === requester) requesters.remove(fieldId)
    }

    /** The requester of the field [fieldId], or null when that field isn't shown. */
    fun find(fieldId: String): FocusRequester? = requesters[fieldId]
}
