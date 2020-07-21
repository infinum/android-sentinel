package com.infinum.designer.ui.models

enum class ColorModel(val type: String) {
    HEX("hex"),
    RGB("rgb"),
    HSV("hsv");

    companion object {

        operator fun invoke(type: String) =
            ColorModel.values().firstOrNull { it.type == type }
    }
}