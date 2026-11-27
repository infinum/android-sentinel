package com.infinum.sentinel.ui.annotations.overlay

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers the fallback used when Compose's own resolver cannot read the composition:
 * [ViewTargetResolver] walks the `View` hierarchy and stops at the Compose host, so Compose's
 * interop internals such as `AndroidViewsHandler` are never reported as the target.
 *
 * Compose is not on the `androidTest` classpath, so these tests stand in a plain `ViewGroup` for
 * the Compose host through the `isOpaqueHost` parameter of [ViewTargetNode] and [ViewTargetResolver].
 */
@RunWith(AndroidJUnit4::class)
internal class ComposeHostFallbackTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    /** Stands in for Compose's internal `AndroidComposeView`. */
    private class FakeComposeHost(context: Context) : FrameLayout(context)

    private fun laidOut(root: FrameLayout, width: Int = 200, height: Int = 200): FrameLayout {
        root.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        root.layout(0, 0, width, height)
        return root
    }

    @Test
    @JvmName("resolveStopsAtTheComposeHostRatherThanDescendingIntoItsInteropChildren")
    fun `resolve stops at the compose host rather than descending into its interop children`() {
        // Stands in for AndroidViewsHandler: an interop child nested inside the Compose host that
        // must never itself be reported as the resolved target.
        val interopChild = TextView(context).apply { text = "interop child" }
        val host =
            FakeComposeHost(context).apply {
                addView(interopChild, FrameLayout.LayoutParams(100, 100))
            }
        val root = laidOut(FrameLayout(context).apply { addView(host, FrameLayout.LayoutParams(100, 100)) })

        val resolver = ViewTargetResolver(isOpaqueHost = { it is FakeComposeHost })
        val resolved = resolver.resolve(root, 5, 5)!!

        assertEquals("FakeComposeHost", resolved.node.type)
        assertTrue(resolved.node.children.isEmpty())
        assertTrue(resolved.handle === host)
    }

    @Test
    @JvmName("theComposeHostNodeHasNoChildrenEvenThoughItsViewDoes")
    fun `the compose host node has no children even though its view does`() {
        val host =
            FakeComposeHost(context).apply {
                addView(TextView(context), FrameLayout.LayoutParams(50, 50))
                addView(TextView(context), FrameLayout.LayoutParams(50, 50))
            }
        val root = laidOut(FrameLayout(context).apply { addView(host) })

        val node = root.toTargetNode(parent = null, isOpaqueHost = { it is FakeComposeHost })
        val hostNode = node.children.single()

        assertEquals("FakeComposeHost", hostNode.type)
        assertTrue(hostNode.children.isEmpty())
    }

    /**
     * The app-visible `ComposeView` (here a `FrameLayout`) carries the id declared in XML, while
     * the Compose host inside it has no id. The host is not annotatable, so
     * [TargetSelection.nearestAnnotatable] climbs to its parent and picks up that id.
     */
    @Test
    @JvmName("resolveReportsTheWrappingViewsIdWhenTheHostItselfHasNone")
    fun `resolve reports the wrapping view's id when the host itself has none`() {
        val host = FakeComposeHost(context).apply { addView(TextView(context), FrameLayout.LayoutParams(100, 100)) }
        val composeViewStandIn =
            FrameLayout(context).apply {
                id = com.infinum.sentinel.R.id.fab
                addView(host, FrameLayout.LayoutParams(100, 100))
            }
        val root = laidOut(FrameLayout(context).apply { addView(composeViewStandIn) })

        val resolver = ViewTargetResolver(isOpaqueHost = { it is FakeComposeHost })
        val resolved = resolver.resolve(root, 5, 5)!!

        assertEquals("fab", resolved.node.viewId)
        assertEquals("FrameLayout", resolved.node.type)
    }
}
