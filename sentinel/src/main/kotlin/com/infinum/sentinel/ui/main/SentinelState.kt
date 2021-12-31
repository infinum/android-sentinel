package com.infinum.sentinel.ui.main

import android.graphics.drawable.Drawable

internal sealed class SentinelState {

    data class ApplicationIconAndName(
        val icon: Drawable,
        val name: String
    ) : SentinelState()
}
