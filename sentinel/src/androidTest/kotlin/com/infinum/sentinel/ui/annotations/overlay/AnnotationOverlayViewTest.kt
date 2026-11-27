package com.infinum.sentinel.ui.annotations.overlay

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinum.sentinel.annotations.AnnotationToolkit
import com.infinum.sentinel.annotations.SentinelAnnotation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AnnotationOverlayViewTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun annotation(id: String = "a1") =
        SentinelAnnotation(
            id = id,
            note = "note",
            toolkit = AnnotationToolkit.VIEW,
            targetType = "Button",
            targetPath = "Button",
        )

    private fun tapAt(view: AnnotationOverlayView, x: Float, y: Float): Boolean {
        val now = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0)
        val handled = view.dispatchTouchEvent(down)
        down.recycle()
        return handled
    }

    @Test
    @JvmName("captureModeConsumesEveryTouch")
    fun `capture mode consumes every touch`() {
        val view = AnnotationOverlayView(context).apply { captureActive = true }

        assertTrue(tapAt(view, 5f, 5f))
    }

    @Test
    @JvmName("passiveModeLetsTouchesThrough")
    fun `passive mode lets touches through`() {
        val view = AnnotationOverlayView(context).apply { captureActive = false }

        assertFalse(tapAt(view, 5f, 5f))
    }

    @Test
    @JvmName("passiveModeConsumesATouchOnABadge")
    fun `passive mode consumes a touch on a badge`() {
        val view =
            AnnotationOverlayView(context).apply {
                captureActive = false
                markers = listOf(AnnotationMarker(1, Bounds(0, 0, 100, 100), annotation()))
            }

        assertTrue(tapAt(view, 5f, 5f))
    }

    @Test
    @JvmName("passiveModeReportsTheTappedBadge")
    fun `passive mode reports the tapped badge`() {
        var tapped: SentinelAnnotation? = null
        val view =
            AnnotationOverlayView(context).apply {
                captureActive = false
                markers = listOf(AnnotationMarker(1, Bounds(0, 0, 100, 100), annotation("target")))
                onBadgeTapped = { tapped = it }
            }

        tapAt(view, 5f, 5f)

        assertEquals("target", tapped?.id)
    }

    @Test
    @JvmName("markerAtMissesAPointOutsideTheBadgeSquare")
    fun `markerAt misses a point outside the badge square`() {
        val view =
            AnnotationOverlayView(context).apply {
                markers = listOf(AnnotationMarker(1, Bounds(0, 0, 100, 100), annotation()))
            }

        assertNull(view.markerAt(90, 90))
    }

    @Test
    @JvmName("markerAtPrefersTheMostRecentlyAddedBadgeWhenTwoOverlap")
    fun `markerAt prefers the most recently added badge when two overlap`() {
        val view =
            AnnotationOverlayView(context).apply {
                markers =
                    listOf(
                        AnnotationMarker(1, Bounds(0, 0, 100, 100), annotation("older")),
                        AnnotationMarker(2, Bounds(0, 0, 100, 100), annotation("newer")),
                    )
            }

        assertEquals("newer", view.markerAt(5, 5)?.annotation?.id)
    }
}
