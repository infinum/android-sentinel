package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import com.chuckerteam.chucker.api.Chucker
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around Chucker.
 *
 * Tool Activity will launch with FLAG_ACTIVITY_SINGLE_TOP and FLAG_ACTIVITY_NEW_TASK flags.
 */
public data class ChuckerTool(
    private val listener: View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Chucker.getLaunchIntent(it.context).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
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
    override fun name(): Int = R.string.sentinel_network

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
