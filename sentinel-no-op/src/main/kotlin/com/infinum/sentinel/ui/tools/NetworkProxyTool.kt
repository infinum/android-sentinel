package com.infinum.sentinel.ui.tools

import android.view.View
import androidx.annotation.StringRes
import com.infinum.sentinel.Sentinel

@Suppress("UnusedPrivateMember")
public data class NetworkProxyTool(
    @StringRes private val name: Int = 0,
    private val listener: View.OnClickListener =
        View.OnClickListener {
            // no - op
        },
) : Sentinel.NetworkTool {
    override fun icon(): Int? = null

    override fun name(): Int = 0

    override fun listener(): View.OnClickListener =
        View.OnClickListener {
            // no - op
        }
}
