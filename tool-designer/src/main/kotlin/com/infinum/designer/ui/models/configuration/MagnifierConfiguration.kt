package com.infinum.designer.ui.models.configuration

import com.infinum.designer.ui.models.ColorModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MagnifierConfiguration(
    override var enabled: Boolean = false,
    val colorModel: ColorModel = ColorModel.HEX
) : AbstractConfiguration()