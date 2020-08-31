package com.infinum.sentinel.domain.repository

import android.content.Context
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.PreferencesCollector
import com.infinum.sentinel.data.sources.raw.ToolsCollector

internal class CollectorRepository(context: Context) {

    val device: DeviceCollector = DeviceCollector()

    val application: ApplicationCollector = ApplicationCollector(context)

    val permissions: PermissionsCollector = PermissionsCollector(context)

    val preferences: PreferencesCollector = PreferencesCollector(context)

    lateinit var tools: ToolsCollector

    fun setup(tools: Set<Sentinel.Tool>) {
        this.tools = ToolsCollector(tools)
    }
}
