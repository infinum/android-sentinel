package com.infinum.designer.ui.listeners

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

class NodeTouchListener(
    private val onActionDown: (MotionEvent) -> Unit,
    private val onActionMove: (MotionEvent) -> Unit,
    private val onActionOther: () -> Unit
) : View.OnTouchListener {

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onActionOther()
        }
        return true
    }
}