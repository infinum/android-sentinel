package com.infinum.sentinel

import android.view.View
import androidx.annotation.StringRes

@Suppress("unused")
class Sentinel private constructor(
    @Suppress("UNUSED_PARAMETER") tools: Set<Tool> = setOf()
) {

    companion object {

        @JvmStatic
        @JvmOverloads
        fun watch(tools: Set<Tool> = setOf()): Sentinel =
            lazyOf(Sentinel(tools)).value

        @JvmStatic
        fun show() = Unit
    }

    interface Tool {

        @StringRes
        fun name(): Int

        fun listener(): View.OnClickListener
    }

    @Suppress("unused")
    interface NetworkTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = 0
    }

    @Suppress("unused")
    interface MemoryTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = 0
    }

    @Suppress("unused")
    interface AnalyticsTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = 0
    }

    @Suppress("unused")
    interface DatabaseTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = 0
    }

    @Suppress("unused")
    interface ReportTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = 0
    }

    @Suppress("unused")
    interface BluetoothTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = 0
    }

    @Suppress("unused")
    interface DistributionTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = 0
    }

    @Suppress("unused")
    interface DesignTool : Tool {

        /**
         * A stub for this tool
         *
         * @return a String resource that will be used to generate a name for a Button in Tools UI
         */
        override fun name(): Int = 0
    }
}
