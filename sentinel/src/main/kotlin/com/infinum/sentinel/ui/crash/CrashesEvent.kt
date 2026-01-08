package com.infinum.sentinel.ui.crash

import com.infinum.sentinel.data.models.local.CrashEntity

internal sealed class CrashesEvent {
    data class CrashesIntercepted(
        val value: List<CrashEntity>,
    ) : CrashesEvent()
}
