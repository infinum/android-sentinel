package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around LeakCanary in no - op.
 *
 */
public class LeakCanaryTool : Sentinel.Tool {
    override fun name(): Int = 0

    override fun listener(): View.OnClickListener =
        View.OnClickListener {
            // no - op
        }
}
