package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.thimble.Thimble

/**
 * Specific wrapper tool around Thimble.
 *
 * Tool Activity will launch with no additional flags.
 */
public data class ThimbleTool(
    private val listener: View.OnClickListener = View.OnClickListener {
        Thimble.show()
    }
) : Sentinel.Tool {

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
    override fun name(): Int = R.string.sentinel_design

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
