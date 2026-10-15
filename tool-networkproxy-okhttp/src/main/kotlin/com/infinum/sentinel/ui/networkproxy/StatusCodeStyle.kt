package com.infinum.sentinel.ui.networkproxy

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.infinum.sentinel.tool.networkproxy.R

internal object StatusCodeStyle {
    @Suppress("MagicNumber")
    @ColorRes
    fun colorFor(code: Int): Int =
        when (code / 100) {
            2 -> R.color.sentinel_network_proxy_status_success
            4 -> R.color.sentinel_network_proxy_status_client_error
            5 -> R.color.sentinel_network_proxy_status_server_error
            else -> R.color.sentinel_network_proxy_status_other
        }

    fun apply(
        view: TextView,
        code: Int,
    ) {
        view.text = code.toString()
        view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, colorFor(code)))
    }
}
