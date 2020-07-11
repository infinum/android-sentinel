package com.infinum.designer.ui.tools

import android.content.Intent
import android.view.View
import com.infinum.sentinel.Sentinel
import com.infinum.designer.ui.DesignerActivity

class DesignerTool : Sentinel.DesignTool {

    override fun listener(): View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Intent(it.context, DesignerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}