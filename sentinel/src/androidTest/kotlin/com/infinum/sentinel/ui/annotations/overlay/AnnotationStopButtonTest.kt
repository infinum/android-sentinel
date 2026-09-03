package com.infinum.sentinel.ui.annotations.overlay

import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression coverage for a bug the plan's Step 5 snippet carried verbatim: `View`'s own
 * `CheckForLongPress` runnable is scheduled on `ACTION_DOWN` and normally cancelled by `View`'s
 * own `ACTION_MOVE`/`ACTION_UP` handling once movement crosses touch slop. `AnnotationStopButton`
 * stops delegating to `super.onTouchEvent` the moment a drag starts (it returns `true` itself
 * instead), so that cancellation never ran — the pending long-press fired anyway, roughly 500ms
 * after the original `ACTION_DOWN`, regardless of the drag in progress. A slow, deliberate drag
 * to reposition the button — a completely ordinary action — reopened the Sentinel panel mid-drag.
 */
@RunWith(AndroidJUnit4::class)
internal class AnnotationStopButtonTest {
    private fun motionEvent(action: Int, x: Float, y: Float, downTime: Long): MotionEvent =
        MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0)

    private fun attachedButton(activity: android.app.Activity, onReopen: () -> Unit): AnnotationStopButton {
        val button = AnnotationStopButton(activity).apply { this.onReopen = onReopen }
        (activity.window.decorView as ViewGroup).addView(
            button,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT),
        )
        return button
    }

    @Test
    @JvmName("aSlowDragPastTheLongPressTimeoutDoesNotReopenTheSentinelPanel")
    fun `a slow drag past the long-press timeout does not reopen the Sentinel panel`() {
        var reopened = false
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        ActivityScenario.launch(AnnotationHostTestActivity::class.java).use { scenario ->
            lateinit var button: AnnotationStopButton
            scenario.onActivity { activity -> button = attachedButton(activity) { reopened = true } }
            instrumentation.waitForIdleSync()

            val downTime = SystemClock.uptimeMillis()
            val slop = ViewConfiguration.get(button.context).scaledTouchSlop
            instrumentation.runOnMainSync {
                button.dispatchTouchEvent(motionEvent(MotionEvent.ACTION_DOWN, 0f, 0f, downTime))
            }
            instrumentation.runOnMainSync {
                button.dispatchTouchEvent(motionEvent(MotionEvent.ACTION_MOVE, (slop + 20).toFloat(), 0f, downTime))
            }

            // Hold the drag well past the long-press timeout before releasing — this is where the
            // stray CheckForLongPress used to fire.
            Thread.sleep(ViewConfiguration.getLongPressTimeout() * 2L)

            instrumentation.runOnMainSync {
                button.dispatchTouchEvent(motionEvent(MotionEvent.ACTION_UP, (slop + 20).toFloat(), 0f, downTime))
            }
            instrumentation.waitForIdleSync()

            assertFalse(reopened)
        }
    }

    /**
     * Guards against a fix that over-corrects by disabling long-press reopening outright: a real
     * press-and-hold with no movement must still invoke [AnnotationStopButton.onReopen].
     */
    @Test
    @JvmName("aGenuineLongPressWithoutMovementReopensTheSentinelPanel")
    fun `a genuine long-press without movement reopens the Sentinel panel`() {
        var reopened = false
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        ActivityScenario.launch(AnnotationHostTestActivity::class.java).use { scenario ->
            lateinit var button: AnnotationStopButton
            scenario.onActivity { activity -> button = attachedButton(activity) { reopened = true } }
            instrumentation.waitForIdleSync()

            val downTime = SystemClock.uptimeMillis()
            instrumentation.runOnMainSync {
                button.dispatchTouchEvent(motionEvent(MotionEvent.ACTION_DOWN, 0f, 0f, downTime))
            }

            Thread.sleep(ViewConfiguration.getLongPressTimeout() * 2L)

            instrumentation.runOnMainSync {
                button.dispatchTouchEvent(motionEvent(MotionEvent.ACTION_UP, 0f, 0f, downTime))
            }
            instrumentation.waitForIdleSync()

            assertTrue(reopened)
        }
    }
}
