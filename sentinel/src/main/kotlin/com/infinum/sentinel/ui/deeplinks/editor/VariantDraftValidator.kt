package com.infinum.sentinel.ui.deeplinks.editor

import com.infinum.sentinel.domain.deeplinks.templates.LinkSpecMatcher

/**
 * Checks a [VariantDraft] before it is saved.
 */
internal object VariantDraftValidator {
    /**
     * Checks [draft] against [form]. An empty key is an error when the row has a value, an empty value is fine,
     * and a blank row is ignored. A required host or path must be filled. A template link that doesn't match its pattern only gets a warning; [matcher] is not
     * asked for links that aren't templates.
     */
    internal fun validate(
        draft: VariantDraft,
        form: VariantEditorForm,
        matcher: LinkSpecMatcher,
    ): VariantDraftValidation {
        val isHostMissing = form.hostMode == FieldMode.REQUIRED && draft.host.isBlank()
        val isPathMissing = form.pathMode == FieldMode.REQUIRED && draft.path.isBlank()
        val spec = form.spec
        val isPatternMismatch =
            spec != null &&
                spec.isTemplate &&
                !isHostMissing &&
                !isPathMissing &&
                matcher.matches(spec, VariantDraftMapper.toLink(draft, form)) == false
        return VariantDraftValidation(
            emptyKeyRowIds = draft.rows.filter { it.key.isBlank() && it.value.isNotBlank() }.map { it.rowId }.toSet(),
            isHostMissing = isHostMissing,
            isPathMissing = isPathMissing,
            isPatternMismatch = isPatternMismatch,
        )
    }
}
