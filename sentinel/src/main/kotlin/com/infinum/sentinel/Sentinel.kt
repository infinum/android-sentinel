package com.infinum.sentinel

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.domain.Domain
import com.infinum.sentinel.ui.SentinelActivity

class Sentinel private constructor(
    private val context: Context,
    tools: Set<Tool> = setOf()
) {

    companion object {

        private var INSTANCE: Sentinel? = null

        @JvmStatic
        @JvmOverloads
        fun watch(context: Context, tools: Set<Tool> = setOf()): Sentinel {
            if (INSTANCE == null) {
                INSTANCE = Sentinel(context, tools)
            }
            return INSTANCE as Sentinel
        }
    }

    init {
        Domain.initialise(context, tools) { showNow() }
    }

    /**
     * Used for manually showing Sentinel UI
     */
    fun show() {
        val manualTrigger = ManualTrigger()
        if (manualTrigger.active) {
            showNow()
        }
    }

    private fun showNow() {
        context.startActivity(
            Intent(context, SentinelActivity::class.java)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
        )
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
