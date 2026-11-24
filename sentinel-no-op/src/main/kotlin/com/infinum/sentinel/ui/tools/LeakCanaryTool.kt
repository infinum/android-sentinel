package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around LeakCanary in no - op.
 *
 */
@Suppress("UnusedPrivateMember")
public data class LeakCanaryTool(
    private val listener: View.OnClickListener =
        View.OnClickListener {
            // no - op
        },
) : Sentinel.Tool {
    override fun icon(): Int? = null

    override fun name(): Int = 0

    override fun listener(): View.OnClickListener =
        View.OnClickListener {
            // no - op
        }
}
