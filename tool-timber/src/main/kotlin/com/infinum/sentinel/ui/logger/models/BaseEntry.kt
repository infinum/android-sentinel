package com.infinum.sentinel.ui.logger.models

import org.json.JSONObject

internal open class BaseEntry(
    open val level: Level,
    open val timestamp: Long,
    open val tag: String? = null,
    open val message: String? = null,
    open val stackTrace: String? = null
) {

    fun asString(): String =
        JSONObject()
            .put("level", level)
            .put("timestamp", timestamp)
            .put("tag", tag)
            .put("message", message)
            .put("stackTrace", stackTrace)
            .toString()
}
