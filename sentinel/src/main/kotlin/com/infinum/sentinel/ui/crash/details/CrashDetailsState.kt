package com.infinum.sentinel.ui.crash.details

import com.infinum.sentinel.data.models.local.CrashEntity

internal sealed class CrashDetailsState {
    data class Data(
        val value: CrashEntity,
    ) : CrashDetailsState()
}
