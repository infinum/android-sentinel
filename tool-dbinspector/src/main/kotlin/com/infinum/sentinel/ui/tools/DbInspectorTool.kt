package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import com.infinum.sentinel.Sentinel
import im.dino.dbinspector.activities.DbInspectorActivity

class DbInspectorTool : Sentinel.DatabaseTool {

    override fun listener(): View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Intent(it.context, DbInspectorActivity::class.java)
        )
    }
}
