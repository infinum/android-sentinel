package com.infinum.sentinel.di

import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.models.memory.triggers.airplanemode.AirplaneModeOnTrigger
import com.infinum.sentinel.data.models.memory.triggers.foreground.ForegroundTrigger
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.data.models.memory.triggers.shake.ShakeTrigger
import com.infinum.sentinel.data.models.memory.triggers.usb.UsbConnectedTrigger
import com.infinum.sentinel.data.sources.local.room.SentinelDatabase
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.BasicCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.PreferencesCollector
import com.infinum.sentinel.data.sources.raw.ToolsCollector
import com.infinum.sentinel.domain.repository.TriggersRepository
import org.koin.dsl.module
import java.util.concurrent.Executors

object SentinelComponent {

    fun modules(tools: Set<Sentinel.Tool>, showNow: () -> Unit) =
        listOf(
            data(),
            repositories(),
            triggers(showNow),
            collectors(tools)
        )

    private fun data() =
        module {
            single { SentinelDatabase.create(get()) }
            single { Executors.newSingleThreadExecutor() }
        }

    private fun repositories() =
        module {
            single {
                TriggersRepository(get(), get())
            }
        }

    fun triggers(showNow: () -> Unit) =
        module {
            single { ManualTrigger() }
            single { ForegroundTrigger { showNow() } }
            single { ShakeTrigger(get()) { showNow() } }
            single { UsbConnectedTrigger(get()) { showNow() } }
            single { AirplaneModeOnTrigger(get()) { showNow() } }
        }

    private fun collectors(tools: Set<Sentinel.Tool>) =
        module {
            single { ToolsCollector(tools) }
            single { BasicCollector(get()) }
            single { ApplicationCollector(get()) }
            single { PermissionsCollector(get()) }
            single { DeviceCollector() }
            single { PreferencesCollector(get()) }
        }
}