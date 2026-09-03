package com.infinum.sentinel.ui.annotations.overlay

import android.view.View
import com.infinum.sentinel.annotations.AnnotationToolkit
import com.infinum.sentinel.annotations.SentinelAnnotation

/**
 * A resolved selection: the toolkit-independent description plus the live handle used to
 * recompute bounds while the note is on screen.
 */
internal data class ResolvedTarget(
    val node: TargetNode,
    val handle: Any,
    val toolkit: AnnotationToolkit,
)

internal interface TargetResolver {
    /** The annotatable target at ([x], [y]) in screen coordinates, or `null`. */
    fun resolve(root: View, x: Int, y: Int): ResolvedTarget?

    /** The live element [annotation] should now point at after its screen was recreated. */
    fun rebind(annotation: SentinelAnnotation, root: View): ResolvedTarget?
}
