package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.ui.networkemuator.NetworkEmulatorActivity

/**
 * A tool that allows developers to emulate slow and flaky network conditions
 * for testing how the application behaves under poor connectivity.
 *
 * This tool provides an easy-to-use interface to configure:
 * - Network delay (milliseconds)
 * - Failure rate (percentage of requests that should fail)
 * - Variance in network response times
 *
 */
public data class NetworkEmulatorTool(
    @StringRes private val name: Int = R.string.sentinel_network_emulator,
    private val listener: View.OnClickListener =
        View.OnClickListener {
            it.context.startActivity(
                Intent(it.context, NetworkEmulatorActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        },
) : Sentinel.NetworkTool {
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
    override fun name(): Int = name

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
