package com.infinum.sentinel.domain.networkproxy

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal object JsonFormatter {
    private const val INDENT = 2

    // Null means the text is not JSON, the caller decides how to tell the user
    @Suppress("SwallowedException")
    fun format(text: String): String? {
        val trimmed = text.trim()
        return try {
            if (trimmed.startsWith("[")) JSONArray(trimmed).toString(INDENT) else JSONObject(trimmed).toString(INDENT)
        } catch (error: JSONException) {
            null
        }
    }
}
