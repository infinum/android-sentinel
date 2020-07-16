package com.infinum.designer.ui.overlays.grid

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import com.infinum.designer.extensions.dpToPx
import com.infinum.designer.ui.overlays.AbstractOverlay
import com.infinum.designer.ui.utils.ViewUtils
import com.infinum.designer.ui.views.grid.GridView

class GridOverlay(
    private val context: Context
) : AbstractOverlay(context) {

    private var gridOverlayView: GridView? = null

    private var horizontalGridLineColor: Int = 0
    private var verticalGridLineColor: Int = 0
    private var horizontalGridSize: Int = 1
    private var verticalGridSize: Int = 1

    override fun show() {
        gridOverlayView = GridView(context)

        gridOverlayView?.let {
            windowManager.addView(
                it,
                WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewUtils.getWindowType(),
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
                )
            )
            it.updateHorizontalColor(horizontalGridLineColor)
            it.updateVerticalColor(verticalGridLineColor)
            it.updateHorizontalSize(horizontalGridSize)
            it.updateVerticalSize(verticalGridSize)
        }

        showing = true
    }

    override fun hide() {
        gridOverlayView?.let { removeViewIfAttached(it) }
        gridOverlayView = null

        showing = false
    }

    override fun update(bundle: Bundle) {
        horizontalGridLineColor = bundle.getInt("horizontal_grid_line_color", 0)
        verticalGridLineColor = bundle.getInt("vertical_grid_line_color", 0)
        horizontalGridSize = bundle.getInt("horizontal_grid_size", 0)
        verticalGridSize = bundle.getInt("vertical_grid_size", 0)

        gridOverlayView?.let {
            it.updateHorizontalColor(horizontalGridLineColor)
            it.updateVerticalColor(verticalGridLineColor)
            it.updateHorizontalSize(horizontalGridSize)
            it.updateVerticalSize(verticalGridSize)
        }
    }

    override fun reset() {
        horizontalGridLineColor = Color.RED
        verticalGridLineColor = Color.BLUE
        horizontalGridSize = 8.0f.dpToPx(context)
        verticalGridSize = 8.0f.dpToPx(context)
        super.reset()
    }
}