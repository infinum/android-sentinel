package com.infinum.sentinel.ui.logger.models

import java.text.SimpleDateFormat
import java.util.Date
import org.json.JSONObject

internal open class BaseEntry(
    open val level: Level,
    open val timestamp: Long,
    open val tag: String? = null,
    open val message: String? = null,
    open val stackTrace: String? = null
) {
    fun asJSONString(): String =
        JSONObject()
            .put("level", level)
            .put("timestamp", timestamp)
            .put("tag", tag)
            .put("message", message)
            .put("stackTrace", stackTrace)
            .toString()

    fun asLineString(dateTimeFormat: SimpleDateFormat): String =
        buildString {
            append(dateTimeFormat.format(Date(timestamp)))
            append(" LEVEL: ")
            append(level)
            append(" TAG: ")
            append(tag.orEmpty())
            append(" MESSAGE: ")
            append(message.orEmpty())
            append(" STACKTRACE: ")
            append(stackTrace.orEmpty())
            append(System.lineSeparator())
        }
}
