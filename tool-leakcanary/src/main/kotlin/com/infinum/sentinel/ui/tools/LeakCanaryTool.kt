package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import leakcanary.LeakCanary

/**
 * Specific wrapper tool around LeakCanary.
 * In the moment of tool initialisation dumping heap by LeakCanary is disabled.
 * When LeakCanary UI is invoked, heap is dumped and enabled per session.
 *
 * Tool Activity will launch with no additional flags.
 */
public data class LeakCanaryTool(
    private val listener: View.OnClickListener =
        View.OnClickListener {
            LeakCanary.config = LeakCanary.config.copy(dumpHeap = true)
            LeakCanary.dumpHeap()
            it.context.startActivity(LeakCanary.newLeakDisplayActivityIntent())
        },
) : Sentinel.Tool {
    init {
        LeakCanary.showLeakDisplayActivityLauncherIcon(false)
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)
    }

    /**
     * An optional icon for this tool
     *
     * @return a Drawable resource that will be used to generate an icon in a Button in Tools UI
     */
    override fun icon(): Int? = null

    /**
     * A dedicated name for this tool
     *
     * @return a String resource that will be used to generate a name for a Button in Tools UI
     */
    override fun name(): Int = R.string.sentinel_memory

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
