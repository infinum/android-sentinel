package com.infinum.sentinel.ui.networkproxy

import androidx.annotation.StringRes
import com.infinum.sentinel.domain.networkproxy.models.ProxyRule
import com.infinum.sentinel.domain.networkproxy.models.RecentCall

internal sealed class NetworkProxyListItem {
    data class Header(@StringRes val title: Int) : NetworkProxyListItem()

    data class Empty(@StringRes val message: Int) : NetworkProxyListItem()

    data class Rule(val rule: ProxyRule) : NetworkProxyListItem()

    data class Recent(val call: RecentCall) : NetworkProxyListItem()
}
