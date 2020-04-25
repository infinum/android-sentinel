package com.infinum.sentinel.di

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
import com.infinum.sentinel.domain.repository.FormatsRepository
import com.infinum.sentinel.domain.repository.TriggersRepository
import com.infinum.sentinel.ui.formatters.HtmlStringBuilder
import com.infinum.sentinel.ui.formatters.JsonStringBuilder
import com.infinum.sentinel.ui.formatters.MarkdownStringBuilder
import com.infinum.sentinel.ui.formatters.PlainStringBuilder
import com.infinum.sentinel.ui.formatters.XmlStringBuilder
import com.infinum.sentinel.ui.tools.AppInfoTool
import org.koin.dsl.module
import java.util.concurrent.Executors

internal object KoinDI {
    val database = module {
        single { SentinelDatabase.create(get()) }
        single { Executors.newSingleThreadExecutor() }
    }

    val repositories = module {
        single { TriggersRepository(get(), get()) }
        single { FormatsRepository(get(), get()) }
    }

    val triggers = module {
        single { ManualTrigger().apply { active = true } }
//        single { ForegroundTrigger { showNow() } }
//        single { ShakeTrigger(get()) { showNow() } }
//        single { UsbConnectedTrigger(get()) { showNow() } }
//        single { AirplaneModeOnTrigger(get()) { showNow() } }
    }

    val collectors = module {
//        single { ToolsCollector(tools.plus(AppInfoTool())) }
        single { BasicCollector(get()) }
        single { ApplicationCollector(get()) }
        single { PermissionsCollector(get()) }
        single { DeviceCollector() }
        single { PreferencesCollector(get()) }
    }

    val formatters = module {
        single { PlainStringBuilder(get(), get(), get(), get(), get()) }
        single { MarkdownStringBuilder(get(), get(), get(), get(), get()) }
        single { JsonStringBuilder(get(), get(), get(), get(), get()) }
        single { XmlStringBuilder(get(), get(), get(), get(), get()) }
        single { HtmlStringBuilder(get(), get(), get(), get(), get()) }
    }

    val modules = listOf(
        database,
        repositories,
        triggers,
        collectors,
        formatters
    )
}