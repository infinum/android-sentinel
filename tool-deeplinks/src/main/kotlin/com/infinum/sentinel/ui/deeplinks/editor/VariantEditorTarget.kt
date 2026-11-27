package com.infinum.sentinel.ui.deeplinks.editor

import kotlinx.serialization.Serializable

/**
 * What the editor opens for. Serializable, because it is part of the editor's navigation key.
 */
@Serializable
internal sealed interface VariantEditorTarget {
    /** A new variant of the base link with [baseKey], declared by [componentName] if it is a declared link. */
    @Serializable
    data class NewFromBase(
        val baseKey: String,
        val componentName: String?,
    ) : VariantEditorTarget

    /** The saved variant with [variantId]. */
    @Serializable
    data class Existing(
        val variantId: String,
    ) : VariantEditorTarget
}
