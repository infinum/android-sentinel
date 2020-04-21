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
class CollarTool : Sentinel.AnalyticsTool {

    override fun listener(): View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            CollarUi.launchIntent(it.context).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
    }
}
