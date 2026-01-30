package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import com.infinum.collar.ui.CollarUi
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.tool.collar.R

/**
 * Specific wrapper tool around Collar.
 *
 * Tool Activity will launch with FLAG_ACTIVITY_SINGLE_TOP flag.
 */
public data class CollarTool(
    private val listener: View.OnClickListener =
        View.OnClickListener {
            it.context.startActivity(
                CollarUi.launchIntent().apply {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                },
            )
        },
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
    override fun name(): Int = R.string.sentinel_analytics

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
