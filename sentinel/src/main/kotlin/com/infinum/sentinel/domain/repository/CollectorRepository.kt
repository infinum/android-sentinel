package com.infinum.sentinel.domain.repository

import android.content.Context
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.BasicCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.PreferencesCollector
import com.infinum.sentinel.data.sources.raw.ToolsCollector

internal object CollectorRepository {

    private lateinit var basicCollector: BasicCollector
    private lateinit var deviceCollector: DeviceCollector
    private lateinit var applicationCollector: ApplicationCollector
    private lateinit var permissionsCollector: PermissionsCollector
    private lateinit var preferencesCollector: PreferencesCollector
    private lateinit var toolsCollector: ToolsCollector

    fun initialise(
        context: Context,
        tools: Set<Sentinel.Tool>
    ) {
        basicCollector = BasicCollector(context)
        deviceCollector = DeviceCollector()
        applicationCollector = ApplicationCollector(context)
        permissionsCollector = PermissionsCollector(context)
        preferencesCollector = PreferencesCollector(context)
        toolsCollector = ToolsCollector(tools)
    }

    fun basic() = basicCollector

    fun device() = deviceCollector

    fun application() = applicationCollector

    fun permissions() = permissionsCollector

    fun preferences() = preferencesCollector

    fun tools() = toolsCollector
}