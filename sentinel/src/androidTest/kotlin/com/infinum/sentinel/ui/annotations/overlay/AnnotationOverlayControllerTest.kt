package com.infinum.sentinel.ui.annotations.overlay

import android.app.Activity
import android.app.Application
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinum.sentinel.data.sources.memory.annotations.InMemoryAnnotationCache
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AnnotationOverlayControllerTest {
    private var controllerUnderTest: AnnotationOverlayController? = null

    private fun overlayCount(activity: Activity): Int {
        val decor = activity.window.decorView as ViewGroup
        return (0 until decor.childCount).count { decor.getChildAt(it) is AnnotationOverlayView }
    }

    private fun laidOut(root: FrameLayout, width: Int = 200, height: Int = 200): FrameLayout {
        root.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        root.layout(0, 0, width, height)
        return root
    }

    /**
     * `start` registers the controller on the Application for the rest of the process's life —
     * correct in production, where exactly one controller is ever created, but this instrumented
     * test process is shared across every test method in this class. Without unregistering here,
     * a controller left over from an earlier test keeps reacting to later tests' activities and
     * attaches an overlay they don't expect.
     */
    @After
    fun tearDown() {
        controllerUnderTest?.let {
            ApplicationProvider.getApplicationContext<Application>().unregisterActivityLifecycleCallbacks(it)
        }
    }

    @Test
    @JvmName("noOverlayIsAttachedWhileCaptureIsOffAndThereAreNoNotes")
    fun `no overlay is attached while capture is off and there are no notes`() {
        val cache = InMemoryAnnotationCache()
        val controller = AnnotationOverlayController(cache)
        controllerUnderTest = controller
        controller.start(ApplicationProvider.getApplicationContext())

        ActivityScenario.launch(AnnotationHostTestActivity::class.java).use { scenario ->
            scenario.onActivity { assertEquals(0, overlayCount(it)) }
        }
    }

    /**
     * Named as a plain identifier rather than the usual backtick-plus-`@JvmName` pair: `runTest`
     * compiles its suspend lambda to a real inner class (coroutines can't use `invokedynamic` the
     * way an ordinary lambda can), and that class is named after the *source* name of this
     * function. `@JvmName` renames the method itself but not that inner class, so a backtick name
     * with spaces here still fails D8 with "Space characters ... are not allowed prior to DEX
     * version 040" even with `@JvmName` present.
     */
    @Test
    fun armingCaptureAttachesTheOverlayToTheResumedActivity() = runTest {
        val cache = InMemoryAnnotationCache()
        val controller = AnnotationOverlayController(cache)
        controllerUnderTest = controller
        controller.start(ApplicationProvider.getApplicationContext())

        ActivityScenario.launch(AnnotationHostTestActivity::class.java).use { scenario ->
            cache.active.value = true
            scenario.onActivity { activity ->
                activity.window.decorView.post { assertEquals(1, overlayCount(activity)) }
            }
        }
    }

    /** See [armingCaptureAttachesTheOverlayToTheResumedActivity] for why this isn't a backtick name. */
    @Test
    fun disarmingCaptureWithNoNotesDetachesTheOverlay() = runTest {
        val cache = InMemoryAnnotationCache()
        val controller = AnnotationOverlayController(cache)
        controllerUnderTest = controller
        controller.start(ApplicationProvider.getApplicationContext())

        ActivityScenario.launch(AnnotationHostTestActivity::class.java).use { scenario ->
            cache.active.value = true
            cache.active.value = false
            scenario.onActivity { activity ->
                activity.window.decorView.post { assertEquals(0, overlayCount(activity)) }
            }
        }
    }

    /**
     * Regression test for a bug the brief's own `handleCaptureTap` had: broadening a selection
     * via `TargetSelection.stepUp` built the resulting `ResolvedTarget` by reusing the
     * *narrower*, first-tapped element's live handle instead of resolving a handle for the
     * broadened node itself. A note saved after broadening would then silently keep pointing at
     * — and its marker drawn around — the original narrower element forever, since `boundsOf`'s
     * fast path trusts any live, attached `View` handle without checking it still matches the
     * saved `targetType`.
     *
     * This calls the private `stepUpTarget` directly via reflection rather than driving
     * `handleCaptureTap` end to end through a real attached overlay. Driving it end to end turned
     * out to be blocked by a separate, pre-existing defect this test's development uncovered:
     * `AnnotationOverlayView` is added as the *last* child of `decorView` and covers the whole
     * screen, so once it is attached, `ViewTargetResolver.resolve(host.window.decorView, x, y)`
     * always resolves *every* tap to the overlay's own (non-annotatable, childless) node before
     * ever reaching the real content underneath — `deepestAt` finds the overlay first because it
     * draws on top, and having no children of its own it returns immediately without descending
     * into its siblings. That defect is real, independent of the bug this test targets, and out
     * of scope for this test-only change (see the fix report) — reflection is the fallback this
     * task's review explicitly allowed for exactly this situation.
     */
    @Test
    @JvmName("stepUpTargetUsesTheBroadenedNodesOwnHandleNotTheNarrowerOnes")
    fun `stepUpTarget uses the broadened node's own handle, not the narrower one's`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val leaf = TextView(context).apply { text = "Leaf" }
        val container =
            laidOut(
                FrameLayout(context).apply {
                    isClickable = true
                    addView(leaf, FrameLayout.LayoutParams(100, 100))
                },
            )

        val leafTarget = ViewTargetResolver().resolve(container, 5, 5)!!
        assertSame(leaf, leafTarget.handle)

        val controller = AnnotationOverlayController(InMemoryAnnotationCache())
        val stepUpTarget =
            AnnotationOverlayController::class.java
                .getDeclaredMethod("stepUpTarget", ResolvedTarget::class.java)
                .apply { isAccessible = true }

        val broadened = stepUpTarget.invoke(controller, leafTarget) as ResolvedTarget?

        assertEquals("FrameLayout", broadened?.node?.type)
        assertSame(container, broadened?.handle)
    }
}
