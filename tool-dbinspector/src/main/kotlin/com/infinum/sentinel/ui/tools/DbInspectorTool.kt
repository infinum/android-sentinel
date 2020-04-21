package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import com.infinum.sentinel.Sentinel
import im.dino.dbinspector.activities.DbInspectorActivity
/**
 * Specific wrapper tool around DbInspector.
 *
 * Tool Activity will launch with no additional flags.
 */
class DbInspectorTool : Sentinel.DatabaseTool {

    override fun listener(): View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Intent(it.context, DbInspectorActivity::class.java)
        )
    }
}
