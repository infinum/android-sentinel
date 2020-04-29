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
data class DbInspectorTool(
    private val listener: View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Intent(it.context, DbInspectorActivity::class.java)
        )
    }
) : Sentinel.DatabaseTool {

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
