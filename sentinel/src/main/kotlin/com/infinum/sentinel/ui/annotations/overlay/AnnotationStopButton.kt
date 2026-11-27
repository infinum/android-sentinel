package com.infinum.sentinel.ui.annotations.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.infinum.sentinel.R

/**
 * The draggable control that ends capture.
 *
 * A tap ends capture. A long press reopens the Sentinel panel with capture still armed.
 *
 * Implements [SentinelOverlayView] so target resolution skips it and taps resolve to the app
 * underneath.
 */
@SuppressLint("ViewConstructor")
internal class AnnotationStopButton(
    context: Context,
) : FloatingActionButton(ContextThemeWrapper(context, R.style.Sentinel_Theme)),
    SentinelOverlayView {
    private var downX = 0f
    private var downY = 0f
    private var dragging = false

    var onStop: (() -> Unit)? = null
    var onReopen: (() -> Unit)? = null

    init {
        setImageResource(R.drawable.sentinel_ic_close)
        contentDescription = context.getString(R.string.sentinel_annotations_stop)
        setOnClickListener { onStop?.invoke() }
        setOnLongClickListener {
            onReopen?.invoke()
            true
        }
    }

    /** Drags follow the finger; a tap still reaches the click listener. */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var consumed = false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX - translationX
                downY = event.rawY - translationY
                dragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downX
                val dy = event.rawY - downY
                if (!dragging &&
                    (kotlin.math.abs(dx - translationX) > touchSlop || kotlin.math.abs(dy - translationY) > touchSlop)
                ) {
                    dragging = true
                    // super.onTouchEvent is not called while dragging (see below), so the
                    // long-press check View scheduled on ACTION_DOWN is cancelled here.
                    cancelLongPress()
                }
                if (dragging) {
                    translationX = dx
                    translationY = dy
                    consumed = true
                }
            }

            MotionEvent.ACTION_UP -> consumed = dragging
        }
        return consumed || super.onTouchEvent(event)
    }

    private val touchSlop: Int
        get() = android.view.ViewConfiguration.get(context).scaledTouchSlop

    companion object {
        fun visibilityFor(captureActive: Boolean): Int = if (captureActive) View.VISIBLE else View.GONE
    }
}
