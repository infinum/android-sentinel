package com.infinum.sentinel

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.ui.Presentation

public object Sentinel {

    @JvmStatic
    @JvmOverloads
    public fun watch(tools: Set<Tool> = setOf()): Sentinel {
        Presentation.setup(tools) { Presentation.show() }
        return this
    }

    @JvmStatic
    public fun show(): Unit = showInternal()

    /**
     * Used for manually showing Sentinel UI
     */
    private fun showInternal() {
        val manualTrigger = ManualTrigger()
        if (manualTrigger.active) {
            Presentation.show()
        }
    }

    @Suppress("unused")
    public interface Tool {

        /**
         * An optional icon for this tool
         *
         * @return a Drawable resource that will be used to generate an icon in a Button in Tools UI
         */
        @DrawableRes
        public fun icon(): Int? = null

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        public fun name(): Int

        /**
         * A callback to be invoked when this view is clicked.
         *
         * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
         */
        public fun listener(): View.OnClickListener
    }

    @Suppress("unused")
    public interface NetworkTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = R.string.sentinel_network
    }

    @Suppress("unused")
    public interface MemoryTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = R.string.sentinel_memory
    }

    @Suppress("unused")
    public interface AnalyticsTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = R.string.sentinel_analytics
    }

    @Suppress("unused")
    public interface DatabaseTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = R.string.sentinel_database
    }

    @Suppress("unused")
    public interface ReportTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = R.string.sentinel_report
    }

    @Suppress("unused")
    public interface BluetoothTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = R.string.sentinel_bluetooth
    }

    @Suppress("unused")
    public interface DistributionTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = R.string.sentinel_distribution
    }

    @Suppress("unused")
    public interface DesignTool : Tool {

        /**
         * A dedicated name for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = R.string.sentinel_design
    }
}
