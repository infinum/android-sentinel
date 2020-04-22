package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import com.chuckerteam.chucker.api.Chucker
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around Chucker.
 *
 * Tool Activity will launch with FLAG_ACTIVITY_SINGLE_TOP and FLAG_ACTIVITY_NEW_TASK flags.
 */
class ChuckerTool : Sentinel.NetworkTool {

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Chucker.getLaunchIntent(it.context, Chucker.SCREEN_HTTP).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
