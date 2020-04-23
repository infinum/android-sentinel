package com.infinum.sentinel.ui

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal interface SentinelFeatures {

    fun settings()

    fun device()

    fun application()

    fun permissions()

    fun preferences()

    fun tools()

    fun share()
}