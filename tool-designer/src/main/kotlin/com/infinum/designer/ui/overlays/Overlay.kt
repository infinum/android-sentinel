package com.infinum.designer.ui.overlays

import com.infinum.designer.ui.models.configuration.Configuration

interface Overlay<T: Configuration> {

    fun show()

    fun hide()

    fun isShowing(): Boolean

    fun update(configuration: T)

    fun reset(configuration: T)
}