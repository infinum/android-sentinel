package com.infinum.sentinel.ui.annotations.overlay

import android.view.View
import com.infinum.sentinel.annotations.AnnotationToolkit
import com.infinum.sentinel.annotations.SentinelAnnotation

/**
 * [isOpaqueHost] defaults to [ComposeSupport.isComposeHost]. See [ViewTargetNode] for why it is a
 * parameter.
 */
internal class ViewTargetResolver(
    private val isOpaqueHost: (View) -> Boolean = ComposeSupport::isComposeHost,
) : TargetResolver {
    override fun resolve(root: View, x: Int, y: Int): ResolvedTarget? {
        val tree = root.toTargetNode(parent = null, isOpaqueHost = isOpaqueHost)
        val deepest = TargetSelection.deepestAt(tree, x, y) ?: return null
        val selected = TargetSelection.nearestAnnotatable(deepest) ?: deepest
        return ResolvedTarget(selected, (selected as ViewTargetNode).view, AnnotationToolkit.VIEW)
    }

    /**
     * Re-points [annotation] at the element now painted where it was captured, searching from the
     * deepest hit up through its ancestors. Accepts only a same-type element that also matches the
     * captured id, or, when the element has no id, the captured text. A field that was not captured
     * does not constrain the match, so a missing value never orphans a note.
     */
    override fun rebind(annotation: SentinelAnnotation, root: View): ResolvedTarget? {
        val tree = root.toTargetNode(parent = null, isOpaqueHost = isOpaqueHost)
        val node = TargetSelection.ancestorAt(tree, annotation.rebindX, annotation.rebindY) { it.matches(annotation) }
        return node?.let { ResolvedTarget(it, (it as ViewTargetNode).view, AnnotationToolkit.VIEW) }
    }

    private fun TargetNode.matches(annotation: SentinelAnnotation): Boolean =
        type == annotation.targetType &&
            (annotation.viewId == null || viewId == annotation.viewId) &&
            (annotation.viewId != null || annotation.text == null || text == annotation.text)
}
