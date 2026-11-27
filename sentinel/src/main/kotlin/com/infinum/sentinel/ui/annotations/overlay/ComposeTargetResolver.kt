package com.infinum.sentinel.ui.annotations.overlay

import android.view.View
import androidx.compose.ui.tooling.data.UiToolingDataApi
import com.infinum.sentinel.annotations.AnnotationToolkit
import com.infinum.sentinel.annotations.SentinelAnnotation

internal class ComposeTargetResolver : TargetResolver {
    @OptIn(UiToolingDataApi::class)
    override fun resolve(root: View, x: Int, y: Int): ResolvedTarget? {
        val tree = ComposeSupport.rootNodeOf(root) ?: return null
        val deepest = TargetSelection.deepestAt(tree, x, y) ?: return null
        val selected = TargetSelection.nearestAnnotatable(deepest) ?: return null
        return ResolvedTarget(selected, (selected as ComposeTargetNode).group, AnnotationToolkit.COMPOSE)
    }

    /**
     * Compose recreates its node objects on every recomposition, so a saved group handle is
     * never reusable. Re-binding searches from the deepest hit at the captured point up through
     * its ancestors, and accepts a match on source location, which is stable across
     * recomposition, and on the test tag.
     */
    @OptIn(UiToolingDataApi::class)
    override fun rebind(annotation: SentinelAnnotation, root: View): ResolvedTarget? {
        val tree = ComposeSupport.rootNodeOf(root) ?: return null
        val node = TargetSelection.ancestorAt(tree, annotation.rebindX, annotation.rebindY) { it.matches(annotation) }
        return node?.let { ResolvedTarget(it, (it as ComposeTargetNode).group, AnnotationToolkit.COMPOSE) }
    }

    private fun TargetNode.matches(annotation: SentinelAnnotation): Boolean =
        type == annotation.targetType &&
            (annotation.sourceLocation == null || sourceLocation == annotation.sourceLocation) &&
            (annotation.testTag == null || testTag == annotation.testTag)
}
