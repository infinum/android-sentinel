package com.infinum.sentinel.data.sources.raw.collectors

import android.content.Context
import android.provider.Settings
import com.infinum.sentinel.data.models.raw.DeviceData
import com.infinum.sentinel.domain.collectors.Collectors

internal class DeviceCollector(
    private val context: Context
) : Collectors.Device {

    override fun invoke() = DeviceData(
        autoTime = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AUTO_TIME,
            0
        ) == 1,
        autoTimezone = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AUTO_TIME_ZONE,
            0
        ) == 1
    )
}
