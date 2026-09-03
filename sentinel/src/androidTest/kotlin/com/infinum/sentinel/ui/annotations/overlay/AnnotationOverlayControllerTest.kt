package com.infinum.sentinel.ui.annotations.overlay

import android.app.Activity
import android.app.Application
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinum.sentinel.data.sources.memory.annotations.InMemoryAnnotationCache
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AnnotationOverlayControllerTest {
    private var controllerUnderTest: AnnotationOverlayController? = null

    private fun overlayCount(activity: Activity): Int {
        val decor = activity.window.decorView as ViewGroup
        return (0 until decor.childCount).count { decor.getChildAt(it) is AnnotationOverlayView }
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
}
