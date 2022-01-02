package com.infinum.sentinel

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

@Suppress("unused")
public object Sentinel {

    @JvmStatic
    @JvmOverloads
    @Suppress("UNUSED_PARAMETER")
    public fun watch(tools: Set<Tool> = setOf()): Sentinel = this

    @JvmStatic
    public fun show(): Unit = Unit

    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    public fun setExceptionHandler(handler: Thread.UncaughtExceptionHandler?): Unit = Unit

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
