package com.infinum.sentinel.sample

import android.content.Intent
import android.view.View
import co.infinum.collar.ui.CollarUi
import com.chuckerteam.chucker.api.Chucker
import com.infinum.sentinel.Sentinel
import im.dino.dbinspector.activities.DbInspectorActivity

class DbInspectorTool : Sentinel.DatabaseTool {

    override fun listener(): View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Intent(it.context, DbInspectorActivity::class.java)
        )
    }
}