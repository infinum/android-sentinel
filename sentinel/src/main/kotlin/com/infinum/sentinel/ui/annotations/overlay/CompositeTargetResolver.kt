package com.infinum.sentinel.ui.annotations.overlay

import android.view.View
import android.view.ViewGroup
import com.infinum.sentinel.annotations.SentinelAnnotation

/**
 * Routes a tap to the Compose resolver when it lands inside a composition, and to the View
 * resolver otherwise.
 *
 * When Compose is present but unreadable (an unsupported version, or `ui-tooling-data` absent),
 * the Compose resolver returns `null` and the View resolver yields the hosting `ComposeView`, so
 * the element stays annotatable with no source location. The fallback is silent because it is
 * the normal outcome on current Compose versions (see the README).
 */
internal class CompositeTargetResolver(
    private val views: TargetResolver = ViewTargetResolver(),
    private val compose: TargetResolver = ComposeTargetResolver(),
) : TargetResolver {
    override fun resolve(root: View, x: Int, y: Int): ResolvedTarget? {
        val host = composeHostAt(root, x, y)
        if (host != null) {
            val resolved = compose.resolve(host, x, y)
            if (resolved != null) return resolved
        }
        return views.resolve(root, x, y)
    }

    override fun rebind(annotation: SentinelAnnotation, root: View): ResolvedTarget? {
        val host = composeHostAt(root, annotation.rebindX, annotation.rebindY)
        if (host != null) {
            val rebound = compose.rebind(annotation, host)
            if (rebound != null) return rebound
        }
        return views.rebind(annotation, root)
    }

    /** The deepest Compose host view containing the point, or `null`. */
    private fun composeHostAt(root: View, x: Int, y: Int): View? {
        if (!ComposeSupport.isAvailable) return null
        if (!root.containsOnScreen(x, y)) return null
        if (root is ViewGroup) {
            for (index in root.childCount - 1 downTo 0) {
                val child = root.getChildAt(index)
                if (child.visibility != View.VISIBLE) continue
                val hit = composeHostAt(child, x, y)
                if (hit != null) return hit
            }
        }
        return if (ComposeSupport.isComposeHost(root)) root else null
    }

    private fun View.containsOnScreen(x: Int, y: Int): Boolean {
        val location = IntArray(2)
        getLocationOnScreen(location)
        return x >= location[0] && x < location[0] + width && y >= location[1] && y < location[1] + height
    }
}
