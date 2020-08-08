package com.infinum.sentinel.data.sources.raw

import com.infinum.sentinel.data.models.raw.DeviceData

internal class DeviceCollector : Collector<DeviceData> {

    override fun invoke() = DeviceData()
}
