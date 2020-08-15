package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel
import com.infinum.thimble.Thimble

/**
 * Specific wrapper tool around Thimble.
 *
 * Tool Activity will launch with no additional flags.
 */
data class ThimbleTool(
    private val listener: View.OnClickListener = View.OnClickListener {
        Thimble.show()
    }
) : Sentinel.DesignTool {

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
