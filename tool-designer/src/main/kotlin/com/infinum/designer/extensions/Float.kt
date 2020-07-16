package com.infinum.designer.extensions

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

fun Float.dpToPx(context: Context): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics
    ).toInt()

fun Float?.orZero(): Float = this ?: 0.0f