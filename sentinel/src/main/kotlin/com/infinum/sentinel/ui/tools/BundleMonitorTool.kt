package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.ui.bundlemonitor.BundleMonitorActivity

/**
 * Specific wrapper tool that monitors Bundle sizes for the application which implemented Sentinel.
 */
internal data class BundleMonitorTool(
    @StringRes private val name: Int = R.string.sentinel_bundle_monitor,
    private val listener: View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Intent(
                it.context,
                BundleMonitorActivity::class.java
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
    }
) : Sentinel.Tool {

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
