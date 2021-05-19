package com.infinum.sentinel.extensions

import android.annotation.SuppressLint
import java.util.Locale

@SuppressLint("DefaultLocale")
internal fun String.sanitize() = this.lowercase(Locale.getDefault()).replace(" ", "_")
