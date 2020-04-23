package com.infinum.sentinel.data.sources.raw

import com.infinum.sentinel.data.models.raw.DeviceData

internal class DeviceCollector : AbstractCollector<DeviceData>() {

    override lateinit var data: DeviceData

    override fun collect() {
        data = DeviceData()
    }
}
