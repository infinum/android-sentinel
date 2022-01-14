package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around Timber.
 *
 * Tool Activity will launch with no additional flags.
 */
public class TimberTool : Sentinel.Tool {

    override fun name(): Int = 0

    override fun listener(): View.OnClickListener = View.OnClickListener {
        // no - op
    }
}
