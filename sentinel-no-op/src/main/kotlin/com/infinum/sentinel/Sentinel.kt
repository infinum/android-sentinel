package com.infinum.sentinel

import android.view.View
import androidx.annotation.StringRes

@Suppress("unused")
public object Sentinel {

    @JvmStatic
    @JvmOverloads
    @Suppress("UNUSED_PARAMETER")
    public fun watch(tools: Set<Tool> = setOf()): Sentinel = this

    @JvmStatic
    public fun show(): Unit = Unit

    public interface Tool {

        @StringRes
        public fun name(): Int

        public fun listener(): View.OnClickListener
    }

    @Suppress("unused")
    public interface NetworkTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = 0
    }

    @Suppress("unused")
    public interface MemoryTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = 0
    }

    @Suppress("unused")
    public interface AnalyticsTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = 0
    }

    @Suppress("unused")
    public interface DatabaseTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = 0
    }

    @Suppress("unused")
    public interface ReportTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = 0
    }

    @Suppress("unused")
    public interface BluetoothTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = 0
    }

    @Suppress("unused")
    public interface DistributionTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = 0
    }

    @Suppress("unused")
    public interface DesignTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        @StringRes
        override fun name(): Int = 0
    }
}
