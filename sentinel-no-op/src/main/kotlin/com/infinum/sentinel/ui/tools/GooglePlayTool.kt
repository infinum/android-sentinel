package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel

class GooglePlayTool : Sentinel.Tool {

    override fun name(): Int = R.string.sentinel_google_play

    override fun listener(): View.OnClickListener = View.OnClickListener { }
}
