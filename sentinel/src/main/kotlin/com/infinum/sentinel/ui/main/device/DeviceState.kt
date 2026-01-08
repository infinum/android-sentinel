package com.infinum.sentinel.ui.main.device

import com.infinum.sentinel.data.models.raw.DeviceData

internal sealed class DeviceState {
    data class Data(
        val value: DeviceData,
    ) : DeviceState()
}
