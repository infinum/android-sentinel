package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.dbinspector.DbInspector
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around DbInspector.
 *
 * Tool Activity will launch with no additional flags.
 */
data class DbInspectorTool(
    private val listener: View.OnClickListener = View.OnClickListener {
        DbInspector.show()
    }
) : Sentinel.DatabaseTool {

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
