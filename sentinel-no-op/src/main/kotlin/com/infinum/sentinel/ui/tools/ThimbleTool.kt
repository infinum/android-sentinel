package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool for Thimble in no - op.
 *
 */
public class ThimbleTool : Sentinel.Tool {

    override fun name(): Int = 0

    override fun listener(): View.OnClickListener = View.OnClickListener {
        // no - op
    }
}
