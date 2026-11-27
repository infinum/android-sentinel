package com.infinum.sentinel.domain.networkproxy

import android.content.Context
import android.content.SharedPreferences
import com.infinum.sentinel.domain.networkproxy.models.ProxyRule

internal class NetworkProxyPreferences(context: Context) : RuleSource {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val lock = Any()
    private var cachedJson: String? = null
    private var cachedRules: List<ProxyRule> = emptyList()

    override var isEnabled: Boolean
        get() = preferences.getBoolean(KEY_ENABLED, false)
        set(value) = preferences.edit().putBoolean(KEY_ENABLED, value).apply()

    override val rules: List<ProxyRule>
        get() {
            val json = preferences.getString(KEY_RULES, null)
            synchronized(lock) {
                if (json != cachedJson) {
                    cachedRules = RuleJson.decode(json)
                    cachedJson = json
                }
                return cachedRules
            }
        }

    fun findRule(id: String): ProxyRule? = rules.firstOrNull { it.id == id }

    fun saveRule(rule: ProxyRule) {
        val current = rules
        val updated =
            if (current.any { it.id == rule.id }) {
                current.map { if (it.id == rule.id) rule else it }
            } else {
                current + rule
            }
        writeRules(updated)
    }

    fun deleteRule(id: String) {
        writeRules(rules.filterNot { it.id == id })
    }

    fun setRuleEnabled(
        id: String,
        enabled: Boolean,
    ) {
        writeRules(rules.map { if (it.id == id) it.copy(enabled = enabled) else it })
    }

    private fun writeRules(rules: List<ProxyRule>) {
        preferences.edit().putString(KEY_RULES, RuleJson.encode(rules)).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "sentinel_network_proxy"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_RULES = "rules"
    }
}
