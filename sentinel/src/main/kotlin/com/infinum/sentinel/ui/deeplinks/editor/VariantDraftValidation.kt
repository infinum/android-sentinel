package com.infinum.sentinel.ui.deeplinks.editor

/**
 * The result of checking a draft.
 *
 * @property emptyKeyRowIds rows with a value but a blank key, which block saving.
 * @property isHostMissing a required host is empty.
 * @property isPathMissing a required path is empty.
 * @property isPatternMismatch the link doesn't match the declared pattern. A warning that doesn't block saving.
 */
internal data class VariantDraftValidation(
    val emptyKeyRowIds: Set<Long>,
    val isHostMissing: Boolean,
    val isPathMissing: Boolean,
    val isPatternMismatch: Boolean,
) {
    /** True when the draft can be saved. A pattern mismatch doesn't block it. */
    val canSave: Boolean
        get() = emptyKeyRowIds.isEmpty() && !isHostMissing && !isPathMissing
}
