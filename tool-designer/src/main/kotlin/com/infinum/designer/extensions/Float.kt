package com.infinum.designer.extensions

import android.content.res.Resources
import kotlin.math.roundToInt

fun Float.toPx(): Int =
    (this * Resources.getSystem().displayMetrics.density).roundToInt()

fun Float?.orZero(): Float = this ?: 0.0f