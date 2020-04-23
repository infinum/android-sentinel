package com.infinum.sentinel.extensions

import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
fun String.sanitize() = this.toLowerCase().replace(" ", "_")