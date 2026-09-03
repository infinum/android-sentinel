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
     * never reusable. Re-binding therefore re-resolves at the captured point and accepts a match
     * on source location, which is stable across recomposition, or on the test tag.
     */
    @Suppress("ReturnCount")
    override fun rebind(annotation: SentinelAnnotation, root: View): ResolvedTarget? {
        val candidate = resolve(root, annotation.rebindX, annotation.rebindY) ?: return null
        val node = candidate.node
        if (node.type != annotation.targetType) return null
        if (annotation.sourceLocation != null && node.sourceLocation != annotation.sourceLocation) return null
        if (annotation.testTag != null && node.testTag != annotation.testTag) return null
        return candidate
    }
}
