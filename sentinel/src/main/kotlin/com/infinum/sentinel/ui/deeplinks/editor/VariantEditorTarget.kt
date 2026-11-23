package com.infinum.sentinel.ui.deeplinks.editor

/**
 * What the editor opens for.
 */
internal sealed interface VariantEditorTarget {
    /** A new variant of the base link with [baseKey], declared by [componentName] if it is a declared link. */
    data class NewFromBase(
        val baseKey: String,
        val componentName: String?,
    ) : VariantEditorTarget

    /** The saved variant with [variantId]. */
    data class Existing(
        val variantId: String,
    ) : VariantEditorTarget
}
