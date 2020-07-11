package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool for UI design in no - op.
 *
 */
class DesignerTool : Sentinel.Tool {

    override fun name(): Int = 0

    override fun listener(): View.OnClickListener = View.OnClickListener {
        // no - op
    }
}
