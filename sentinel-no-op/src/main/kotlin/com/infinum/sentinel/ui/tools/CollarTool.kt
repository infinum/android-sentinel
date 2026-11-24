package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around Collar in no - op.
 *
 */
@Suppress("UnusedPrivateMember")
public data class CollarTool(
    private val listener: View.OnClickListener =
        View.OnClickListener {
            // no - op
        },
) : Sentinel.Tool {
    override fun icon(): Int? = null

    override fun name(): Int = 0

    override fun listener(): View.OnClickListener =
        View.OnClickListener {
            // no - op
        }
}
