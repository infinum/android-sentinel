package com.infinum.designer.extensions

import android.graphics.Color
import java.util.*
import kotlin.math.roundToInt

fun Int?.orZero(): Int = this ?: 0

fun Int.half(): Int = (this / 2.0f).roundToInt()

fun Int.getHexCode(): String {
    val r = Color.red(this)
    val g = Color.green(this)
    val b = Color.blue(this)
    return String.format(Locale.getDefault(), "#%02X%02X%02X", r, g, b)
}

fun Int.getRGBCode(): String {
    val r = Color.red(this)
    val g = Color.green(this)
    val b = Color.blue(this)
    return String.format(Locale.getDefault(), "R: %s\nG: %s\nB: %s", r, g, b)
}

fun Int.getHSVCode(): String {
    val hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    return String.format(Locale.getDefault(), "H: %s\nS: %s\nV: %s", hsv[0], hsv[1], hsv[2])
}