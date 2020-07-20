package com.infinum.designer.extensions

import android.content.Context
import android.graphics.Point

fun Context.screenCenter(): Point = Point(
    resources.displayMetrics.widthPixels.half(),
    resources.displayMetrics.heightPixels.half()
)