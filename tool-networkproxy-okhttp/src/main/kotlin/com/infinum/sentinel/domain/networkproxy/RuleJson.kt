package com.infinum.sentinel.domain.networkproxy

import com.infinum.sentinel.domain.networkproxy.models.HttpMethod
import com.infinum.sentinel.domain.networkproxy.models.ProxyRule
import com.infinum.sentinel.domain.networkproxy.models.RuleMode
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal object RuleJson {
    private const val KEY_ID = "id"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_METHOD = "method"
    private const val KEY_PATH = "pathPattern"
    private const val KEY_MODE = "mode"
    private const val KEY_STATUS_CODE = "statusCode"
    private const val KEY_BODY = "body"
    private const val KEY_CONTENT_TYPE = "contentType"

    fun encode(rules: List<ProxyRule>): String =
        JSONArray()
            .apply { rules.forEach { put(toJson(it)) } }
            .toString()

    // A corrupt payload resets to an empty list by design, there is nothing to recover from a debug preference
    @Suppress("SwallowedException")
    fun decode(json: String?): List<ProxyRule> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { index -> array.optJSONObject(index)?.let(::fromJson) }
        } catch (error: JSONException) {
            emptyList()
        }
    }

    private fun toJson(rule: ProxyRule): JSONObject =
        JSONObject()
            .put(KEY_ID, rule.id)
            .put(KEY_ENABLED, rule.enabled)
            .put(KEY_METHOD, rule.method.name)
            .put(KEY_PATH, rule.pathPattern)
            .put(KEY_MODE, rule.mode.name)
            .put(KEY_STATUS_CODE, rule.statusCode)
            .put(KEY_BODY, rule.body ?: JSONObject.NULL)
            .put(KEY_CONTENT_TYPE, rule.contentType)

    private fun fromJson(json: JSONObject): ProxyRule? {
        val id = json.optString(KEY_ID).takeIf { it.isNotEmpty() } ?: return null
        return ProxyRule(
            id = id,
            enabled = json.optBoolean(KEY_ENABLED, true),
            method = HttpMethod.fromName(json.optString(KEY_METHOD)),
            pathPattern = json.optString(KEY_PATH, "/"),
            mode = RuleMode.fromName(json.optString(KEY_MODE)),
            statusCode = json.optInt(KEY_STATUS_CODE, ProxyRule.DEFAULT_STATUS_CODE),
            body = if (json.isNull(KEY_BODY)) null else json.optString(KEY_BODY),
            contentType = json.optString(KEY_CONTENT_TYPE, ProxyRule.DEFAULT_CONTENT_TYPE),
        )
    }
}
