package com.infinum.sentinel.domain.networkproxy

import com.infinum.sentinel.domain.networkproxy.models.ProxyRule

internal interface RuleSource {
    val isEnabled: Boolean
    val rules: List<ProxyRule>
}
