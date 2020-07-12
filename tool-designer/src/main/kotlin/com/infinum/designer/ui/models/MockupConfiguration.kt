package com.infinum.designer.ui.models

import android.os.Bundle

data class MockupConfiguration(
    val opacity: Float = 0.0f,
    val portraitUri: String? = null,
    val landscapeUri: String? = null
) {
    fun toBundle(): Bundle =
        Bundle().apply {
            putFloat("opacity", opacity / 100.0f)
            putString("portraitUri", portraitUri)
            putString("landscapeUri", landscapeUri)
        }
}