package com.infinum.sentinel.data.sources.raw.collectors

import com.infinum.sentinel.data.models.raw.DeviceData
import com.infinum.sentinel.domain.collectors.Collectors

internal class DeviceCollector : Collectors.Device {

    override fun invoke() = DeviceData()
}
