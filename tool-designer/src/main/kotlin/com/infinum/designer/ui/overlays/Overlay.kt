package com.infinum.designer.ui.overlays

import android.os.Bundle

interface Overlay {

    fun show()

    fun hide()

    fun isShowing(): Boolean

    fun update(bundle: Bundle)

    fun reset()
}