package com.infinum.sentinel.ui.deeplinks.editor

/**
 * How the tester can use a host or path field.
 */
internal enum class FieldMode {
    /** Fixed by the base link. */
    READ_ONLY,

    /** Must be filled in to get a link that matches. */
    REQUIRED,

    /** May be filled in. */
    OPTIONAL,
}
