package com.infinum.sentinel.extensions

import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
internal fun String.sanitize() = this.toLowerCase().replace(" ", "_")
