package com.infinum.sentinel.ui.shared.notification

import android.content.Intent

internal interface IntentFactory {
    fun crash(
        applicationName: String,
        id: Long,
    ): Array<Intent>

    fun certificate(applicationName: String): Array<Intent>
}
