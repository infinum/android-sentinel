package com.infinum.sentinel.ui.deeplinks

import androidx.navigation3.runtime.NavKey
import com.infinum.sentinel.ui.deeplinks.editor.VariantEditorFocus
import com.infinum.sentinel.ui.deeplinks.editor.VariantEditorTarget
import kotlinx.serialization.Serializable

/**
 * The screens of the Deep links tool, as Navigation 3 keys. They are serializable, so the back stack survives
 * configuration changes and process death.
 */
@Serializable
internal sealed interface DeepLinksKey : NavKey {
    /** The list of links, the first screen. */
    @Serializable
    data object Links : DeepLinksKey

    /** The breakdown of [link]. */
    @Serializable
    data class Breakdown(
        val link: String,
    ) : DeepLinksKey

    /** The variant editor for [target], focusing the field named by [focus] when it opens. */
    @Serializable
    data class Editor(
        val target: VariantEditorTarget,
        val focus: VariantEditorFocus,
    ) : DeepLinksKey
}
