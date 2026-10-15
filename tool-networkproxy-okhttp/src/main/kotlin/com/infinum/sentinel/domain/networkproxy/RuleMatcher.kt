package com.infinum.sentinel.domain.networkproxy

import com.infinum.sentinel.domain.networkproxy.models.ProxyRule

internal object RuleMatcher {
    private const val WILDCARD = "*"

    fun firstMatch(
        rules: List<ProxyRule>,
        method: String,
        path: String,
    ): ProxyRule? = rules.firstOrNull { it.enabled && matches(it, method, path) }

    fun matches(
        rule: ProxyRule,
        method: String,
        path: String,
    ): Boolean = rule.method.matches(method) && pathMatches(rule.pathPattern, path)

    fun pathMatches(
        pattern: String,
        path: String,
    ): Boolean {
        val patternSegments = segments(pattern)
        val pathSegments = segments(path)
        return patternSegments.size == pathSegments.size &&
            patternSegments.zip(pathSegments).all { (expected, actual) -> expected == WILDCARD || expected == actual }
    }

    private fun segments(value: String): List<String> = value.substringBefore('?').split('/').filter { it.isNotEmpty() }
}
