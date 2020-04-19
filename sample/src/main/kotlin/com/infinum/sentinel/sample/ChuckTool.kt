package com.infinum.sentinel.sample

import android.content.Intent
import android.view.View
import com.chuckerteam.chucker.api.Chucker
import com.infinum.sentinel.Sentinel

class ChuckTool : Sentinel.NetworkTool {

    override fun listener(): View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Chucker.getLaunchIntent(it.context, Chucker.SCREEN_HTTP).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}