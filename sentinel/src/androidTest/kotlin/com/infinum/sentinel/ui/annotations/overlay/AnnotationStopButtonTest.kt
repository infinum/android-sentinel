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
 * A drag held past the long-press timeout must not reopen the panel. `AnnotationStopButton` stops
 * calling `super.onTouchEvent` once a drag starts, so it cancels `View`'s pending long-press check
 * itself.
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

            // Hold the drag well past the long-press timeout before releasing.
            Thread.sleep(ViewConfiguration.getLongPressTimeout() * 2L)

            instrumentation.runOnMainSync {
                button.dispatchTouchEvent(motionEvent(MotionEvent.ACTION_UP, (slop + 20).toFloat(), 0f, downTime))
            }
            instrumentation.waitForIdleSync()

            assertFalse(reopened)
        }
    }

    /**
     * A press-and-hold with no movement still invokes [AnnotationStopButton.onReopen].
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
