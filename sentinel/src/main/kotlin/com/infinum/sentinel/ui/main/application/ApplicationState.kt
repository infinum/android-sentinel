package com.infinum.sentinel.ui.main.application

import com.infinum.sentinel.data.models.raw.ApplicationData

internal sealed class ApplicationState {
    data class Data(
        val value: ApplicationData,
    ) : ApplicationState()
}
