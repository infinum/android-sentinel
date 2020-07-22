package com.infinum.designer.ui.overlays

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.WindowManager
import com.infinum.designer.ui.models.configuration.Configuration
import com.infinum.designer.ui.models.configuration.GridConfiguration

abstract class AbstractOverlay<T: Configuration>(context: Context) : Overlay<T> {

    protected var showing: Boolean = false

    protected val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    protected fun removeViewIfAttached(view: View) {
        if (view.isAttachedToWindow) {
            windowManager.removeView(view)
        }
    }

    override fun isShowing() = showing
}