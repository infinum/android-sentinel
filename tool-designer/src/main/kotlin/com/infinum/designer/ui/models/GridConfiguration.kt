package com.infinum.designer.ui.models

import android.os.Bundle

data class GridConfiguration(
    val horizontalLineColor: Int = 0,
    val verticalLineColor: Int = 0,
    val horizontalGridSize: Int = 4,
    val verticalGridSize: Int = 4
) {
    fun toBundle(): Bundle =
        Bundle().apply {
            putInt("horizontal_grid_line_color", horizontalLineColor)
            putInt("vertical_grid_line_color", verticalLineColor)
            putInt("horizontal_grid_size", horizontalGridSize)
            putInt("vertical_grid_size", verticalGridSize)
        }
}