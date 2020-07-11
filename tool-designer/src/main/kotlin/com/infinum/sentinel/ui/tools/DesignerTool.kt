package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel

class DesignerTool : Sentinel.DesignTool {

    override fun listener(): View.OnClickListener = View.OnClickListener {
        println("Open Designer tools")
    }
}