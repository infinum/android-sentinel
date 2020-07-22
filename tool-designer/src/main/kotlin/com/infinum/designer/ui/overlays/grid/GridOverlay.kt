package com.infinum.designer.ui.overlays.grid

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.ViewGroup
import android.view.WindowManager
import com.infinum.designer.extensions.dpToPx
import com.infinum.designer.ui.models.configuration.GridConfiguration
import com.infinum.designer.ui.overlays.AbstractOverlay
import com.infinum.designer.ui.utils.ViewUtils
import com.infinum.designer.ui.views.grid.GridView

class GridOverlay(
    private val context: Context
) : AbstractOverlay<GridConfiguration>(context) {

    private var configuration: GridConfiguration = GridConfiguration(
        horizontalLineColor = Color.RED,
        verticalLineColor = Color.BLUE,
        horizontalGridSize = 8.0f.dpToPx(context),
        verticalGridSize = 8.0f.dpToPx(context)
    )

    private var view: GridView? = null

    override fun show() {
        view = GridView(context)
            .also {
                windowManager.addView(
                    it,
                    WindowManager.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewUtils.getWindowType(),
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    )
                )
                it.updateHorizontalColor(configuration.horizontalLineColor)
                it.updateVerticalColor(configuration.verticalLineColor)
                it.updateHorizontalSize(configuration.horizontalGridSize)
                it.updateVerticalSize(configuration.verticalGridSize)
            }

        showing = true
    }

    override fun hide() {
        view?.let { removeViewIfAttached(it) }
        view = null

        showing = false
    }

    override fun update(configuration: GridConfiguration) {
        this.configuration = configuration

        view?.let {
            it.updateHorizontalColor(configuration.horizontalLineColor)
            it.updateVerticalColor(configuration.verticalLineColor)
            it.updateHorizontalSize(configuration.horizontalGridSize)
            it.updateVerticalSize(configuration.verticalGridSize)
        }
    }

    override fun reset(configuration: GridConfiguration) {
        this.configuration = configuration
        hide()
        show()
    }
}