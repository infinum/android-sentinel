package com.infinum.sentinel.ui.deeplinks.editor

/**
 * Which field the editor focuses when it opens.
 */
internal enum class VariantEditorFocus {
    /** No field. */
    NONE,

    /** The name field, to rename the variant. */
    NAME,

    /** A new empty parameter row. */
    NEW_PARAMETER,
}
