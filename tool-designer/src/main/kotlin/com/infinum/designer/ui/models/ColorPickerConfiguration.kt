package com.infinum.designer.ui.models

import android.os.Bundle

data class ColorPickerConfiguration(
    val colorModel: String? = null
) {
    fun toBundle(): Bundle =
        Bundle().apply {
            putString("colorModel", colorModel)
        }
}