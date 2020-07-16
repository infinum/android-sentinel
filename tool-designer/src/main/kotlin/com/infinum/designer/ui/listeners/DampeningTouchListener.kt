package com.infinum.designer.ui.listeners

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

class DampeningTouchListener(
    private val onActionDown: (MotionEvent) -> Unit,
    private val onActionMove: (MotionEvent, Float) -> Unit
) : View.OnTouchListener {

    companion object {
        private const val DAMPENING_FACTOR_DP = 25.0f
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(
                event,
                DAMPENING_FACTOR_DP * view.context.resources.displayMetrics.density
            )
        }
        return true
    }
}