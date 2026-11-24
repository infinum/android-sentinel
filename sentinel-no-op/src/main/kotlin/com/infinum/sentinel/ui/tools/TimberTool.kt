package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around Timber.
 *
 * Tool Activity will launch with no additional flags.
 */
@Suppress("UnusedPrivateMember")
public data class TimberTool
@JvmOverloads
constructor(
    public val allowedTags: List<String> = emptyList(),
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
