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
 * Covers the fallback that fires when Compose's own resolver returns nothing — an unreadable
 * composition, such as on Compose 1.11.4 where the tooling `Group` tree comes back with every
 * `name`/`location` null (see the C5 report). [ViewTargetResolver] then walks the plain
 * `View` hierarchy instead, and it used to walk straight into Compose's own interop internals
 * (`AndroidViewsHandler` and friends), reporting one of those as the annotated target. A note
 * naming `AndroidViewsHandler` sends a coding agent looking for a class that exists nowhere in
 * the app's source, so [ViewTargetNode.children] now stops descending once it reaches the
 * Compose host, per [ComposeSupport.isComposeHost].
 *
 * Compose is `compileOnly` for this module (see `sentinel/build.gradle.kts`) and is not on the
 * `androidTest` classpath at all, so [ComposeSupport.isComposeHost] can never see a real
 * `AndroidComposeView` here — [ComposeSupport.isAvailable] is permanently `false` in this
 * process because `Class.forName` can't find it. These tests stand in a plain `ViewGroup` for the
 * Compose host, substituted through the `isOpaqueHost` seam both [ViewTargetNode] and
 * [ViewTargetResolver] default to [ComposeSupport.isComposeHost] for, to exercise the exact same
 * traversal-stops-here rule the production code runs. The real `AndroidComposeView` case was
 * verified by hand on a device, tapping into `ComposeSampleActivity` in the `sample` app with
 * capture armed — see the C5 fallback-fix report for the exact type string observed there.
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
     * Mirrors the real shape found on device: the app-visible `ComposeView` (here, a plain
     * `FrameLayout` standing in for it) carries the id the app declared in XML, while the
     * Compose host nested inside it — internally, `AndroidComposeView` — never carries an id of
     * its own (confirmed by inspecting the Compose 1.11.4 `Wrapper_androidKt.setContent`
     * bytecode: it constructs `AndroidComposeView` and adds it via `addView`, with no `setId`
     * call anywhere). Since the host itself is not annotatable, [TargetSelection.nearestAnnotatable]
     * climbs to its parent and picks up that id — the same mechanism [ViewTargetResolver] already
     * used for any other id-less leaf under an id-bearing ancestor. No special-casing was added
     * for Compose to make this work.
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
