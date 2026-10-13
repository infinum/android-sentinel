package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.tool.networkproxy.R
import com.infinum.sentinel.ui.networkproxy.NetworkProxyActivity

public data class NetworkProxyTool(
    @StringRes private val name: Int = R.string.sentinel_network_proxy,
    private val listener: View.OnClickListener =
        View.OnClickListener {
            it.context.startActivity(
                Intent(it.context, NetworkProxyActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        },
) : Sentinel.NetworkTool {
    override fun icon(): Int? = null

    override fun name(): Int = name

    override fun listener(): View.OnClickListener = listener
}
