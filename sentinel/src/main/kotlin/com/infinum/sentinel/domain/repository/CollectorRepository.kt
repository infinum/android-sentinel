package com.infinum.sentinel.domain.repository

import android.content.Context
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.BasicCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.PreferencesCollector
import com.infinum.sentinel.data.sources.raw.ToolsCollector

internal class CollectorRepository(context: Context) {

    private val basicCollector: BasicCollector = BasicCollector(context)

    private val deviceCollector: DeviceCollector = DeviceCollector()

    private val applicationCollector: ApplicationCollector = ApplicationCollector(context)

    private val permissionsCollector: PermissionsCollector = PermissionsCollector(context)

    private val preferencesCollector: PreferencesCollector = PreferencesCollector(context)

    private lateinit var toolsCollector: ToolsCollector

    fun setup(tools: Set<Sentinel.Tool>) {
        toolsCollector = ToolsCollector(tools)
    }

    fun basic() = basicCollector

    fun device() = deviceCollector

    fun application() = applicationCollector

    fun permissions() = permissionsCollector

    fun preferences() = preferencesCollector

    fun tools() = toolsCollector
}
