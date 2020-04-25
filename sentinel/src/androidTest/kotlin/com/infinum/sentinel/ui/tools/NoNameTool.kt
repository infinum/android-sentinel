package com.infinum.sentinel.ui.tools

import android.view.View
import androidx.annotation.StringRes
import com.infinum.sentinel.Sentinel

internal data class NoNameTool(
    @StringRes private val name: Int = 0
) : Sentinel.Tool {

    override fun name() = name

    override fun listener(): View.OnClickListener = View.OnClickListener {
        // no - op
    }
}