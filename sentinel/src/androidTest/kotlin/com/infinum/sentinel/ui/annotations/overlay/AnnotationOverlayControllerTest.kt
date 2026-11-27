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
     * `start` registers the controller on the Application for the life of the process, and the
     * test process is shared by every test here, so each test unregisters its controller.
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
     * Named as a plain identifier: `runTest` compiles its lambda to an inner class named after the
     * function's source name, and D8 rejects spaces in that name even with `@JvmName`.
     *
     * The assertion runs directly inside `onActivity` so a failure is attributed to this test. The
     * controller collects on `Dispatchers.Main.immediate`, so the overlay is already attached.
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
     * See [armingCaptureAttachesTheOverlayToTheResumedActivity] for the naming and assertion style.
     *
     * Capture is armed first so the overlay exists before `detach` has to remove it.
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
     * Broadening a selection resolves a live handle for the broadened node, not the narrower,
     * first-tapped element, so a note saved after broadening points at the broadened element.
     *
     * Calls the private `stepUpTarget` through reflection to isolate that contract from the rest
     * of `handleCaptureTap`.
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
     * The saved ordinal must be the element's real position among its same-type siblings.
     * `ordinalOf` locates the target by node identity, so it must count against the target's own tree.
     */
    @Test
    @JvmName("theSavedOrdinalIsTheElementsRealPosition")
    fun `the saved ordinal is the element's real position`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val first = TextView(context).apply { text = "first" }
        val second = TextView(context).apply { text = "second" }
        val column =
            android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                addView(first, android.widget.LinearLayout.LayoutParams(300, 60))
                addView(second, android.widget.LinearLayout.LayoutParams(300, 60))
            }
        val root = FrameLayout(context).apply { addView(column) }

        val cache = InMemoryAnnotationCache()
        val controller = AnnotationOverlayController(cache)
        val saveNote =
            AnnotationOverlayController::class.java
                .getDeclaredMethod("saveNote", Activity::class.java, ResolvedTarget::class.java, String::class.java)
                .apply { isAccessible = true }

        ActivityScenario.launch(AnnotationHostTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.window.decorView.findViewById<ViewGroup>(android.R.id.content).addView(root)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val location = IntArray(2)
                second.getLocationOnScreen(location)
                val target = ViewTargetResolver().resolve(activity.window.decorView, location[0] + 5, location[1] + 5)!!
                assertEquals("second", target.node.text)
                saveNote.invoke(controller, activity, target, "note on the second one")
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }

        val saved = cache.load().value.single()
        assertEquals(2, saved.ordinal)
        assertEquals(2, saved.ordinalCount)
    }

    /**
     * Repeated taps on one spot must keep broadening up the tree, not flip between two nodes.
     * Walks three levels so a chain anchored to the current selection, not its start, would fail.
     */
    @Test
    @JvmName("repeatedTapsOnTheSameSpotKeepBroadeningUpTheTree")
    fun `repeated taps on the same spot keep broadening up the tree`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val leaf = TextView(context).apply { text = "Leaf" }
        val inner = FrameLayout(context).apply { addView(leaf, FrameLayout.LayoutParams(100, 100)) }
        val outer = FrameLayout(context).apply { addView(inner, FrameLayout.LayoutParams(100, 100)) }
        val root = FrameLayout(context).apply { addView(outer, FrameLayout.LayoutParams(100, 100)) }

        val controller = AnnotationOverlayController(InMemoryAnnotationCache())
        val handleCaptureTap =
            AnnotationOverlayController::class.java
                .getDeclaredMethod("handleCaptureTap", Activity::class.java, Int::class.java, Int::class.java)
                .apply { isAccessible = true }
        val selectedField =
            AnnotationOverlayController::class.java
                .getDeclaredField("selected")
                .apply { isAccessible = true }

        val handles = mutableListOf<Any?>()
        ActivityScenario.launch(AnnotationHostTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.window.decorView.findViewById<ViewGroup>(android.R.id.content).addView(root)
            }
            // Off the main thread: the views must be laid out before their on-screen bounds mean
            // anything, and waitForIdleSync cannot be called from the main thread.
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val location = IntArray(2)
                leaf.getLocationOnScreen(location)
                repeat(3) {
                    handleCaptureTap.invoke(controller, activity, location[0] + 5, location[1] + 5)
                    handles += (selectedField.get(controller) as ResolvedTarget?)?.handle
                }
            }
        }

        assertSame(leaf, handles[0])
        assertSame(inner, handles[1])
        assertSame(outer, handles[2])
    }

    /**
     * With the overlay attached as the last, full-screen child of `decorView`, as
     * `AnnotationOverlayController.attach` does, a tap still resolves to the app content underneath.
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
     * Extends [resolveReachesRealContentThroughAnAttachedOverlay] to all three views Sentinel adds
     * to `decorView`: the capture overlay, the inspect card and the stop button. Each must
     * implement [SentinelOverlayView], or it would be resolved here instead of [content].
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
                // The card starts GONE, and a GONE view is excluded anyway. Making it visible
                // means only SentinelOverlayView keeps it out of the tree.
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
