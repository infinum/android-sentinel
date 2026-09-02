package com.infinum.sentinel.ui.annotations.overlay

import android.view.View
import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit

internal class ViewTargetResolver : TargetResolver {
    override fun resolve(root: View, x: Int, y: Int): ResolvedTarget? {
        val tree = root.toTargetNode(parent = null)
        val deepest = TargetSelection.deepestAt(tree, x, y) ?: return null
        val selected = TargetSelection.nearestAnnotatable(deepest) ?: deepest
        return ResolvedTarget(selected, (selected as ViewTargetNode).view, AnnotationToolkit.VIEW)
    }

    /**
     * Re-points [annotation] at the element now painted where it was captured. Accepts only a
     * same-type match that also matches the captured id, or — when the element has no id — the
     * captured text. Fields that did not resolve on either side stay permissive, so a missing
     * value never orphans a note.
     */
    @Suppress("ReturnCount")
    override fun rebind(annotation: Annotation, root: View): ResolvedTarget? {
        val candidate = resolve(root, annotation.rebindX, annotation.rebindY) ?: return null
        val node = candidate.node
        if (node.type != annotation.targetType) return null
        if (annotation.viewId != null && node.viewId != annotation.viewId) return null
        if (annotation.viewId == null && annotation.text != null && node.text != annotation.text) return null
        return candidate
    }
}
