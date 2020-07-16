package com.infinum.designer.ui.overlays

import android.content.Context
import android.view.View
import android.view.WindowManager

abstract class AbstractOverlay(context: Context) : Overlay {

    protected var showing: Boolean = false

    protected val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    protected fun removeViewIfAttached(view: View) {
        if (view.isAttachedToWindow) {
            windowManager.removeView(view)
        }
    }

    override fun isShowing() = showing

    override fun reset() {
        hide()
        show()
    }
}