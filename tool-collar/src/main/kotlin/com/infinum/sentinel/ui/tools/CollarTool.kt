package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import co.infinum.collar.ui.CollarUi
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around Collar.
 *
 * Tool Activity will launch with FLAG_ACTIVITY_SINGLE_TOP flag.
 */
data class CollarTool(
    private val listener: View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            CollarUi.launchIntent(it.context).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
    }
) : Sentinel.AnalyticsTool {

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
