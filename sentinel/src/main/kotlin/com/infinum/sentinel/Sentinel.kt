package com.infinum.sentinel

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.infinum.sentinel.data.models.local.crash.ProcessThread
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.extensions.asStringArray
import com.infinum.sentinel.ui.Presentation
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.Locale

public object Sentinel {

    @JvmStatic
    @JvmOverloads
    public fun watch(tools: Set<Tool> = setOf()): Sentinel {
        Presentation.setup(tools) { Presentation.show() }
        return this
    }

    @JvmStatic
    public fun show(): Unit = showInternal()

    @JvmStatic
    public fun setExceptionHandler(handler: Thread.UncaughtExceptionHandler?): Unit =
        Presentation.setExceptionHandler(handler)

    @JvmStatic
    public fun setAnrListener(listener: ApplicationNotRespondingListener?): Unit =
        Presentation.setAnrListener(listener)

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

    @Suppress("unused")
    public fun interface ApplicationNotRespondingListener {

        public fun onAppNotResponding(exception: ApplicationNotRespondingException)
    }

    /**
     * [Exception] to represent an ANR. This [Exception]'s stack trace will be the current stack trace of the given [Thread]
     */
    public class ApplicationNotRespondingException(thread: Thread) : Exception("ANR detected.") {

        private val threadStateMap: String
        internal val threadStateList: List<ProcessThread>

        init {
            stackTrace = thread.stackTrace
            threadStateMap = generateProcessMap()
            threadStateList = generateProcessList()
        }

        /**
         * Logs the current process and all its threads
         */
        private fun generateProcessMap(): String {
            val bos = ByteArrayOutputStream()
            val ps = PrintStream(bos)
            printProcessMap(ps)
            return String(bos.toByteArray())
        }

        /**
         * Prints the current process and all its threads
         *
         * @param ps the [PrintStream] to which the
         * info is written
         */
        private fun printProcessMap(ps: PrintStream) {
            // Get all stack traces in the system
            val stackTraces = Thread.getAllStackTraces()
            ps.println("Process map:")
            for (thread in stackTraces.keys) {
                if (!stackTraces[thread].isNullOrEmpty()) {
                    printThread(ps, Locale.getDefault(), thread, stackTraces[thread]!!)
                    ps.println()
                }
            }
        }

        /**
         * Prints the given thread
         * @param ps the [PrintStream] to which the
         * info is written
         * @param l the [Locale] to use
         * @param thread the [Thread] to print
         * @param stack the [Thread]'s stack trace
         */
        private fun printThread(ps: PrintStream, l: Locale, thread: Thread, stack: Array<StackTraceElement>) {
            ps.println(String.format(l, "\t%s (%s)", thread.name, thread.state))
            for (element in stack) {
                element.apply {
                    ps.println(String.format(l, "\t\t%s.%s(%s:%d)", className, methodName, fileName, lineNumber))
                }
            }
        }

        private fun generateProcessList(): List<ProcessThread> {
            val list = arrayListOf<ProcessThread>()
            val stackTraces = Thread.getAllStackTraces()
            for (thread in stackTraces.keys) {
                if (!stackTraces[thread].isNullOrEmpty()) {
                    val process = ProcessThread(thread.name, thread.state.name, stackTraces[thread]!!.asStringArray())
                    list.add(process)
                }
            }
            return list
        }
    }
}
