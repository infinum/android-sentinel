package com.infinum.designer.ui.models.configuration

import android.graphics.Color
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GridConfiguration(
    override val enabled: Boolean = false,
    val horizontalLineColor: Int = Color.RED,
    val verticalLineColor: Int = Color.BLUE,
    val horizontalGridSize: Int = 8,
    val verticalGridSize: Int = 8
) : AbstractConfiguration()