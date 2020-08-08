package com.infinum.sentinel

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.ui.DependencyGraph
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Sentinel private constructor(tools: Set<Tool> = setOf()) {

    companion object {

        private var INSTANCE: Sentinel? = null

        @JvmStatic
        @JvmOverloads
        fun watch(tools: Set<Tool> = setOf()): Sentinel {
            if (INSTANCE == null) {
                INSTANCE = Sentinel(tools)
            }
            return INSTANCE as Sentinel
        }

        @JvmStatic
        fun show() {
            if (INSTANCE == null) {
                INSTANCE = Sentinel()
                INSTANCE?.showInternal()
            } else {
                INSTANCE?.showInternal()
            }
        }
    }

    init {
        GlobalScope.launch {
            DependencyGraph.setup(tools) { DependencyGraph.show() }
        }
    }

    /**
     * Used for manually showing Sentinel UI
     */
    private fun showInternal() {
        val manualTrigger = ManualTrigger()
        if (manualTrigger.active) {
            DependencyGraph.show()
        }
    }

    @Suppress("unused")
    interface Tool {

        /**
         * An optional icon for this tool
         *
         * @return a Drawable resource that will be used to generate an icon in a Button in Tools UI
         */
        @DrawableRes
        fun icon(): Int? = null

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
    interface MemoryTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_memory
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

    @Suppress("unused")
    interface DesignTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = R.string.sentinel_design
    }
}
