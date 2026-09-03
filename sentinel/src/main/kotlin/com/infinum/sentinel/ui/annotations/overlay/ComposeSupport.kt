package com.infinum.sentinel.ui.annotations.overlay

import android.util.SparseArray
import android.view.View
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Composition
import androidx.compose.ui.tooling.data.Group
import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.compose.ui.tooling.data.asTree

/**
 * The reflection boundary for Compose support.
 *
 * Compose exposes no public API for reading a live composition, so this mirrors the approach
 * Square's Radiography uses: pull the [Composition] out of the host view's keyed tags, unwrap it
 * to its [Composer], and read the slot table through the tooling library's [asTree].
 *
 * All of this is version sensitive. Every entry point returns `null` rather than throwing when
 * Compose is missing or shaped differently than expected, and callers fall back to the View
 * resolver.
 */
internal object ComposeSupport {
    private const val ANDROID_COMPOSE_VIEW = "androidx.compose.ui.platform.AndroidComposeView"
    private const val WRAPPED_COMPOSITION = "androidx.compose.ui.platform.WrappedComposition"
    private const val COMPOSITION_IMPL = "androidx.compose.runtime.CompositionImpl"

    /** Whether Compose is on the runtime classpath at all. */
    val isAvailable: Boolean by lazy {
        try {
            Class.forName(ANDROID_COMPOSE_VIEW)
            true
        } catch (ignored: Throwable) {
            false
        }
    }

    /** Whether [view] is the private view type Compose uses to host a composition. */
    fun isComposeHost(view: View): Boolean =
        isAvailable && view.javaClass.name.contains("AndroidComposeView")

    /**
     * The composition under [view] adapted to [TargetNode], or `null` when the composition
     * cannot be read — an unsupported Compose version, or `ui-tooling-data` missing.
     *
     * The catch here is deliberately wide: everything in [groupTreeOf] is reflection against
     * non-public internals, so a changed field name surfaces as [NoSuchFieldException], a changed
     * field type as [ClassCastException], a changed nullability as [NullPointerException], and a
     * changed class hierarchy as [LinkageError]. All of them mean the same thing — this Compose
     * version doesn't match what this code expects — and all of them should degrade to `null`
     * rather than crash the host app.
     */
    @OptIn(UiToolingDataApi::class)
    fun rootNodeOf(view: View): TargetNode? =
        try {
            groupTreeOf(view)?.let { ComposeTargetNode(it, parent = null) }
        } catch (ignored: LinkageError) {
            null
        } catch (ignored: Exception) {
            null
        }

    /**
     * `file:line`, with any leading path stripped so the value matches what a developer would
     * grep for. `null` when either half is missing.
     */
    fun formatSourceLocation(sourceFile: String?, lineNumber: Int): String? {
        if (sourceFile.isNullOrBlank() || lineNumber <= 0) return null
        return "${sourceFile.substringAfterLast('/')}:$lineNumber"
    }

    @OptIn(UiToolingDataApi::class)
    private fun groupTreeOf(view: View): Group? {
        val composition = view.keyedTags().firstIsInstanceOrNull<Composition>() ?: return null
        val composer = composition.unwrap().composerOrNull() ?: return null
        return composer.compositionData.asTree()
    }

    private fun View.keyedTags(): SparseArray<*> {
        val field =
            try {
                View::class.java.getDeclaredField("mKeyedTags").apply { isAccessible = true }
            } catch (ignored: NoSuchFieldException) {
                return SparseArray<Nothing>(0)
            }
        return field.get(this) as? SparseArray<*> ?: SparseArray<Nothing>(0)
    }

    private inline fun <reified T> SparseArray<*>.firstIsInstanceOrNull(): T? {
        for (index in 0 until size()) {
            val value = valueAt(index)
            if (value is T) return value
        }
        return null
    }

    private fun Composition.unwrap(): Composition {
        if (this::class.java.name != WRAPPED_COMPOSITION) return this
        val field = Class.forName(WRAPPED_COMPOSITION).getDeclaredField("original").apply { isAccessible = true }
        return field.get(this) as Composition
    }

    private fun Composition.composerOrNull(): Composer? {
        if (this::class.java.name != COMPOSITION_IMPL) return null
        val field = Class.forName(COMPOSITION_IMPL).getDeclaredField("composer").apply { isAccessible = true }
        return field.get(this) as? Composer
    }
}
