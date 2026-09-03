package com.infinum.sentinel.ui.annotations.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.annotations.Annotation

/**
 * The layer that draws saved notes over the running app and, while capture is armed, takes
 * every touch so a tap selects an element instead of operating the app.
 *
 * Markers are painted on one canvas rather than built as child views: this layer redraws
 * whenever the app scrolls, and a canvas pass is far cheaper than a view tree.
 */
@SuppressLint("ViewConstructor")
internal class AnnotationOverlayView(
    context: Context,
) : View(context) {
    private val badgeSize = resources.getDimensionPixelSize(R.dimen.sentinel_annotation_badge_size)
    private val markerColor = ContextCompat.getColor(context, R.color.sentinel_annotation_marker)

    private val outlinePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = resources.getDimension(R.dimen.sentinel_annotation_outline_width)
            color = markerColor
        }

    private val badgePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = markerColor
        }

    private val badgeTextPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = resources.getDimension(R.dimen.sentinel_annotation_badge_text_size)
            isFakeBoldText = true
        }

    private val selectionPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = resources.getDimension(R.dimen.sentinel_annotation_outline_width) * 2
            color = markerColor
        }

    /** While true this layer swallows every touch and taps select elements. */
    var captureActive: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var markers: List<AnnotationMarker> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    /** Bounds of the element currently selected in capture mode. */
    var selection: Bounds? = null
        set(value) {
            field = value
            invalidate()
        }

    /** Called with the screen coordinates of a tap while capture is armed. */
    var onCaptureTap: ((x: Int, y: Int) -> Unit)? = null

    /** Called when a saved note's badge is tapped. */
    var onBadgeTapped: ((Annotation) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val origin = IntArray(2)
        getLocationOnScreen(origin)

        markers.forEach { marker ->
            val rect =
                RectF(
                    (marker.bounds.left - origin[0]).toFloat(),
                    (marker.bounds.top - origin[1]).toFloat(),
                    (marker.bounds.right - origin[0]).toFloat(),
                    (marker.bounds.bottom - origin[1]).toFloat(),
                )
            canvas.drawRect(rect, outlinePaint)
            canvas.drawRect(rect.left, rect.top, rect.left + badgeSize, rect.top + badgeSize, badgePaint)
            canvas.drawText(
                marker.number.toString(),
                rect.left + badgeSize / 2f,
                rect.top + badgeSize / 2f - (badgeTextPaint.ascent() + badgeTextPaint.descent()) / 2f,
                badgeTextPaint,
            )
        }

        selection?.let {
            canvas.drawRect(
                (it.left - origin[0]).toFloat(),
                (it.top - origin[1]).toFloat(),
                (it.right - origin[0]).toFloat(),
                (it.bottom - origin[1]).toFloat(),
                selectionPaint,
            )
        }
    }

    /**
     * Capture mode consumes everything. Passive mode consumes only a badge tap, so the app
     * underneath stays fully usable while saved notes remain editable.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val screen = IntArray(2)
        getLocationOnScreen(screen)
        val x = event.x.toInt() + screen[0]
        val y = event.y.toInt() + screen[1]
        val marker = markerAt(x, y)

        if (!captureActive && marker == null) return false

        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            if (marker != null) {
                onBadgeTapped?.invoke(marker.annotation)
            } else {
                onCaptureTap?.invoke(x, y)
            }
        }
        return true
    }

    /**
     * The saved note whose badge square covers ([x], [y]) in screen coordinates, most recently
     * added first so the topmost badge wins.
     */
    fun markerAt(x: Int, y: Int): AnnotationMarker? =
        markers.lastOrNull { marker ->
            Bounds(
                marker.bounds.left,
                marker.bounds.top,
                marker.bounds.left + badgeSize,
                marker.bounds.top + badgeSize,
            ).contains(x, y)
        }
}
