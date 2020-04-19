package com.infinum.sentinel.sample

import android.content.Intent
import android.view.View
import co.infinum.collar.ui.CollarUi
import com.chuckerteam.chucker.api.Chucker
import com.infinum.sentinel.Sentinel

class CollarTool : Sentinel.AnalyticsTool {

    override fun listener(): View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            CollarUi.launchIntent(it.context).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
    }
}