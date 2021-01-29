package com.infinum.sentinel.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.models.memory.triggers.airplanemode.AirplaneModeOnTrigger
import com.infinum.sentinel.data.models.memory.triggers.foreground.ForegroundTrigger
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.data.models.memory.triggers.shake.ShakeTrigger
import com.infinum.sentinel.data.models.memory.triggers.usb.UsbConnectedTrigger
import com.infinum.sentinel.data.sources.local.room.SentinelDatabase
import com.infinum.sentinel.data.sources.memory.triggers.TriggersCache
import com.infinum.sentinel.data.sources.memory.triggers.TriggersCacheFactory
import com.infinum.sentinel.data.sources.raw.collectors.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.collectors.DeviceCollector
import com.infinum.sentinel.data.sources.raw.collectors.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.collectors.PreferencesCollector
import com.infinum.sentinel.data.sources.raw.collectors.ToolsCollector
import com.infinum.sentinel.data.sources.raw.formatters.HtmlFormatter
import com.infinum.sentinel.data.sources.raw.formatters.JsonFormatter
import com.infinum.sentinel.data.sources.raw.formatters.MarkdownFormatter
import com.infinum.sentinel.data.sources.raw.formatters.PlainFormatter
import com.infinum.sentinel.data.sources.raw.formatters.XmlFormatter
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.domain.formatters.Formatters
import java.util.Locale
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

internal object Data {

    object Qualifiers {

        object Name {
            val LAMBDA_TRIGGER = StringQualifier("data.qualifiers.name.lambda.trigger")
            val DATABASE = StringQualifier("data.qualifiers.name.database")
        }

        object Path {
            val DATABASE_DEFAULT = StringQualifier("data.qualifiers.path.database.default")
        }
    }

    private var onTriggered: () -> Unit = {}

    fun setup(onTriggered: () -> Unit) {
        this.onTriggered = onTriggered
    }

    fun modules(): List<Module> =
        listOf(
            memory(),
            local(),
            raw()
        )

    private fun local() = module {
        single(qualifier = Qualifiers.Path.DATABASE_DEFAULT) {
            "databases/sentinel_default.db"
        }
        single(qualifier = Qualifiers.Name.DATABASE) {
            val context: Context = get()
            String.format(
                "sentinel_%s.db",
                context.applicationContext
                    .packageName
                    .replace(".", "_")
                    .toLowerCase(Locale.getDefault())
            )
        }
        single {
            Room.databaseBuilder(
                get(),
                SentinelDatabase::class.java,
                get(Qualifiers.Name.DATABASE)
            )
                .allowMainThreadQueries()
                .createFromAsset(get(Qualifiers.Path.DATABASE_DEFAULT))
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                .fallbackToDestructiveMigration()
                .build()
        }
        single { get<SentinelDatabase>().formats() }
        single { get<SentinelDatabase>().triggers() }
    }

    private fun raw() = module {
        single<Collectors.Device> { DeviceCollector(get()) }
        single<Collectors.Application> { ApplicationCollector(get()) }
        single<Collectors.Permissions> { PermissionsCollector(get()) }
        single<Collectors.Preferences> { PreferencesCollector(get()) }
        single<Collectors.Tools> { (tools: Set<Sentinel.Tool>) -> ToolsCollector(tools) }

        single<Formatters.Plain> { PlainFormatter(get(), get(), get(), get(), get()) }
        single<Formatters.Markdown> { MarkdownFormatter(get(), get(), get(), get(), get()) }
        single<Formatters.Json> { JsonFormatter(get(), get(), get(), get(), get()) }
        single<Formatters.Xml> { XmlFormatter(get(), get(), get(), get(), get()) }
        single<Formatters.Html> { HtmlFormatter(get(), get(), get(), get(), get()) }
    }

    private fun memory() = module {
        single(qualifier = Qualifiers.Name.LAMBDA_TRIGGER) { onTriggered }

        single { ManualTrigger() }
        single { ForegroundTrigger(get(qualifier = Qualifiers.Name.LAMBDA_TRIGGER)) }
        single { ShakeTrigger(get(), get(qualifier = Qualifiers.Name.LAMBDA_TRIGGER)) }
        single { UsbConnectedTrigger(get(), get(qualifier = Qualifiers.Name.LAMBDA_TRIGGER)) }
        single { AirplaneModeOnTrigger(get(), get(qualifier = Qualifiers.Name.LAMBDA_TRIGGER)) }

        single<TriggersCache> {
            TriggersCacheFactory(
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }
    }
}
