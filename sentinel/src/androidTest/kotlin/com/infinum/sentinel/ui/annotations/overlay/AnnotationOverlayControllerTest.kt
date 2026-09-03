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
import androidx.test.platform.app.InstrumentationRegistry
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
     *
     * The assertion runs directly inside `onActivity`, not inside a `decorView.post { }`.
     * `onActivity` blocks the test thread until its block has run on the main thread, so an
     * `AssertionError` thrown here is attributed to this test — a posted one would run after
     * `onActivity` had already returned, and could not fail the test at all. Nothing needs to be
     * waited for: the controller collects on `Dispatchers.Main.immediate`, so setting
     * `cache.active` from the main thread has already attached the overlay by the time
     * `onActivity` runs.
     */
    @Test
    fun armingCaptureAttachesTheOverlayToTheResumedActivity() = runTest {
        val cache = InMemoryAnnotationCache()
        val controller = AnnotationOverlayController(cache)
        controllerUnderTest = controller
        controller.start(ApplicationProvider.getApplicationContext())

        ActivityScenario.launch(AnnotationHostTestActivity::class.java).use { scenario ->
            cache.active.value = true
            scenario.onActivity { activity -> assertEquals(1, overlayCount(activity)) }
        }
    }

    /**
     * See [armingCaptureAttachesTheOverlayToTheResumedActivity] for why this isn't a backtick
     * name, and why the assertion is not posted.
     *
     * Arming first is what gives this test teeth: asserting `0` on an Activity that was never
     * armed would pass whatever `detach` did.
     */
    @Test
    fun disarmingCaptureWithNoNotesDetachesTheOverlay() = runTest {
        val cache = InMemoryAnnotationCache()
        val controller = AnnotationOverlayController(cache)
        controllerUnderTest = controller
        controller.start(ApplicationProvider.getApplicationContext())

        ActivityScenario.launch(AnnotationHostTestActivity::class.java).use { scenario ->
            cache.active.value = true
            scenario.onActivity { activity -> assertEquals(1, overlayCount(activity)) }

            cache.active.value = false
            scenario.onActivity { activity -> assertEquals(0, overlayCount(activity)) }
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
     * `handleCaptureTap` end to end through a real attached overlay. When this test was written,
     * driving it end to end was blocked by a separate defect it uncovered: an attached
     * `AnnotationOverlayView` is the last `decorView` child and covers the whole screen, so
     * `deepestAt` resolved *every* tap to the overlay's own childless node before reaching real
     * content. That defect is fixed — commit 08440fa excludes Sentinel's own overlays from the
     * resolver's tree, and [resolveReachesRealContentThroughAllThreeSentinelViews] guards it — so
     * this is now only a note on why the test is shaped this way, not a live warning. Reflection
     * is kept because it isolates `stepUpTarget`'s handle-broadening contract, which is what this
     * test is actually about.
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

    /**
     * Reproduces the real arrangement `AnnotationOverlayController.attach` creates — the overlay
     * as the *last* child of `decorView`, laid on top of and covering real app content — rather
     * than the hand-built, overlay-free `FrameLayout` root every other resolver test uses. None
     * of those other tests could have caught the bug this one is written against: once a
     * full-screen, childless overlay is a `decorView` child, `deepestAt` used to hit it first (it
     * draws on top) and stop there — it has no children to descend into — before ever reaching
     * the real content underneath. Every tap in capture mode resolved to the overlay itself, never
     * the app.
     */
    @Test
    @JvmName("resolveReachesRealContentThroughAnAttachedOverlay")
    fun `resolve reaches real content through an attached overlay`() {
        lateinit var content: TextView
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        ActivityScenario.launch(AnnotationHostTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                content = TextView(activity).apply { text = "Target" }
                activity.setContentView(content)

                val overlay = AnnotationOverlayView(activity)
                (activity.window.decorView as ViewGroup).addView(
                    overlay,
                    FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT),
                )
            }
            instrumentation.waitForIdleSync()

            scenario.onActivity { activity ->
                val location = IntArray(2)
                content.getLocationOnScreen(location)
                val x = location[0] + content.width / 2
                val y = location[1] + content.height / 2

                val resolved = CompositeTargetResolver().resolve(activity.window.decorView, x, y)

                assertEquals("TextView", resolved?.node?.type)
                assertSame(content, resolved?.handle)
            }
        }
    }

    /**
     * Generalises [resolveReachesRealContentThroughAnAttachedOverlay] to all three views Task 10
     * adds to `decorView`: the capture overlay, the inspect card and the stop button. Each one is
     * a full-bleed-or-large, childless-from-the-resolver's-perspective `decorView` child added on
     * top of real content, so if any of them forgot to implement [SentinelOverlayView] it would
     * be handed back here instead of [content] — exactly the Task 9 defect this task's brief
     * warns is only prevented by convention, never enforced by the type system.
     */
    @Test
    @JvmName("resolveReachesRealContentThroughAllThreeSentinelViews")
    fun `resolve reaches real content through all three Sentinel views`() {
        lateinit var content: TextView
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        ActivityScenario.launch(AnnotationHostTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                content = TextView(activity).apply { text = "Target" }
                activity.setContentView(content)

                val decor = activity.window.decorView as ViewGroup
                decor.addView(
                    AnnotationOverlayView(activity),
                    FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT),
                )
                decor.addView(
                    AnnotationInspectCard(activity),
                    FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT),
                )
                // The card starts View.GONE (see AnnotationInspectCard's init) so it is invisible
                // and zero-sized until shown. Forcing it visible here is what makes this a real
                // test of the marker interface: a GONE card would already be excluded from
                // ViewTargetNode.children on its own, without SentinelOverlayView doing anything.
                (decor.getChildAt(decor.childCount - 1) as AnnotationInspectCard).visibility = View.VISIBLE
                decor.addView(
                    AnnotationStopButton(activity),
                    FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT),
                )
            }
            instrumentation.waitForIdleSync()

            scenario.onActivity { activity ->
                val location = IntArray(2)
                content.getLocationOnScreen(location)
                val x = location[0] + content.width / 2
                val y = location[1] + content.height / 2

                val resolved = CompositeTargetResolver().resolve(activity.window.decorView, x, y)

                assertEquals("TextView", resolved?.node?.type)
                assertSame(content, resolved?.handle)
            }
        }
    }
}
