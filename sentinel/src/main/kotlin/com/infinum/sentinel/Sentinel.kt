package com.infinum.sentinel

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import com.infinum.sentinel.data.models.memory.triggers.TriggerType
import com.infinum.sentinel.data.models.memory.triggers.airplanemode.AirplaneModeOnTrigger
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.data.models.memory.triggers.foreground.ForegroundTrigger
import com.infinum.sentinel.data.models.memory.triggers.shake.ShakeTrigger
import com.infinum.sentinel.data.models.memory.triggers.usb.UsbConnectedTrigger
import com.infinum.sentinel.data.sources.local.room.SentinelDatabase
import com.infinum.sentinel.data.sources.local.room.repository.FormatsRepository
import com.infinum.sentinel.data.sources.local.room.repository.TriggersRepository
import com.infinum.sentinel.data.sources.raw.DataSource
import com.infinum.sentinel.ui.SentinelActivity
import com.infinum.sentinel.ui.tools.AppInfoTool

class Sentinel private constructor(
    private val context: Context,
    tools: List<Tool>
) {

    companion object {
        private var INSTANCE: Sentinel? = null

        fun watch(context: Context, tools: List<Tool>): Sentinel {
            if (INSTANCE == null) {
                INSTANCE = Sentinel(context, tools)
            }
            return INSTANCE as Sentinel
        }
    }

    private val manual = ManualTrigger()
    private val foreground = ForegroundTrigger() { showNow() }
    private val shake = ShakeTrigger(context) { showNow() }
    private val usb = UsbConnectedTrigger(context) { showNow() }
    private val airplane = AirplaneModeOnTrigger(context) { showNow() }

    init {
//        val d = SentinelDatabase.create(context)
//        TriggersRepository.initialize(d)
//        FormatsRepository.initialize(d)
//        TriggersRepository.initValues()
//        FormatsRepository.initValues()
//        d.close()

        SentinelDatabase.create(context).run {
            TriggersRepository.initialize(this)
            FormatsRepository.initialize(this)

            TriggersRepository.load().observeForever { triggers ->
                manual.active =
                    triggers.firstOrNull { it.type == TriggerType.MANUAL }?.enabled ?: true
                shake.active =
                    triggers.firstOrNull { it.type == TriggerType.SHAKE }?.enabled ?: true
                foreground.active =
                    triggers.firstOrNull { it.type == TriggerType.FOREGROUND }?.enabled ?: true
                usb.active =
                    triggers.firstOrNull { it.type == TriggerType.USB_CONNECTED }?.enabled ?: true
                airplane.active =
                    triggers.firstOrNull { it.type == TriggerType.AIRPLANE_MODE_ON }?.enabled
                        ?: true
            }
        }

        DataSource.toolsData = tools.plus(AppInfoTool())
    }

    fun show() {
        if (manual.active) {
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

    interface NetworkTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_network
    }

    interface AnalyticsTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_analytics
    }

    interface DatabaseTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_database
    }

    interface ReportTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_report
    }

    interface BluetoothTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_bluetooth
    }

    interface DistributionTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_google_play
    }
}
