package com.infinum.sentinel

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import com.infinum.sentinel.data.models.memory.triggers.TriggerType
import com.infinum.sentinel.data.models.memory.triggers.airplanemode.AirplaneModeOnTrigger
import com.infinum.sentinel.data.models.memory.triggers.foreground.ForegroundTrigger
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.data.models.memory.triggers.shake.ShakeTrigger
import com.infinum.sentinel.data.models.memory.triggers.usb.UsbConnectedTrigger
import com.infinum.sentinel.data.models.raw.DeviceData
import com.infinum.sentinel.data.sources.local.room.SentinelDatabase
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.BasicCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.PreferencesCollector
import com.infinum.sentinel.data.sources.raw.ToolsCollector
import com.infinum.sentinel.domain.repository.FormatsRepository
import com.infinum.sentinel.domain.repository.TriggersRepository
import com.infinum.sentinel.ui.SentinelActivity
import com.infinum.sentinel.ui.formatters.HtmlStringBuilder
import com.infinum.sentinel.ui.formatters.JsonStringBuilder
import com.infinum.sentinel.ui.formatters.MarkdownStringBuilder
import com.infinum.sentinel.ui.formatters.PlainStringBuilder
import com.infinum.sentinel.ui.formatters.XmlStringBuilder
import com.infinum.sentinel.ui.tools.AppInfoTool
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.getKoin
import java.util.concurrent.Executors

class Sentinel private constructor(
    private val context: Context,
    tools: Set<Tool>
) {

    companion object {
        private var INSTANCE: Sentinel? = null

        fun watch(context: Context, tools: Set<Tool>): Sentinel {
            if (INSTANCE == null) {
                INSTANCE = Sentinel(context, tools)
            }
            return INSTANCE as Sentinel
        }
    }

    init {
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
            single { ForegroundTrigger { showNow() } }
            single { ShakeTrigger(get()) { showNow() } }
            single { UsbConnectedTrigger(get()) { showNow() } }
            single { AirplaneModeOnTrigger(get()) { showNow() } }
        }

        val collectors = module {
            single { ToolsCollector(tools.plus(AppInfoTool())) }
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

        startKoin {
            androidContext(context)
            modules(
                database,
                repositories,
                triggers,
                collectors,
                formatters
            )
        }

        observeTriggers()
    }

    /**
     * Used for manually showing Sentinel UI
     */
    fun show() {
        val manualTrigger: ManualTrigger = getKoin().get()
        if (manualTrigger.active) {
            showNow()
        }
    }

    private fun showNow() {
        context.startActivity(
            Intent(context, SentinelActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
    }

    private fun observeTriggers() {
        val triggersRepository: TriggersRepository = getKoin().get()
        triggersRepository.load().observeForever { triggers ->
            triggers.forEach {
                when (it.type) {
                    TriggerType.MANUAL -> {
                        val trigger: ManualTrigger = getKoin().get()
                        trigger.active = it.enabled
                    }
                    TriggerType.FOREGROUND -> {
                        val trigger: ForegroundTrigger = getKoin().get()
                        trigger.active = it.enabled || DeviceData().isProbablyAnEmulator
                    }
                    TriggerType.SHAKE -> {
                        val trigger: ShakeTrigger = getKoin().get()
                        trigger.active = it.enabled
                    }
                    TriggerType.USB_CONNECTED -> {
                        val trigger: UsbConnectedTrigger = getKoin().get()
                        trigger.active = it.enabled
                    }
                    TriggerType.AIRPLANE_MODE_ON -> {
                        val trigger: AirplaneModeOnTrigger = getKoin().get()
                        trigger.active = it.enabled
                    }
                }
            }
        }
    }

    @Suppress("unused")
    interface Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        fun name(): Int

        /**
         * A callback to be invoked when this view is clicked.
         *
         * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
         */
        fun listener(): View.OnClickListener
    }

    @Suppress("unused")
    interface NetworkTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_network
    }

    @Suppress("unused")
    interface AnalyticsTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_analytics
    }

    @Suppress("unused")
    interface DatabaseTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_database
    }

    @Suppress("unused")
    interface ReportTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_report
    }

    @Suppress("unused")
    interface BluetoothTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_bluetooth
    }

    @Suppress("unused")
    interface DistributionTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_google_play
    }
}
