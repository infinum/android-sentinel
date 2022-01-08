package com.infinum.sentinel.ui.logger.models

internal open class BaseEntry(
    open val level: Level,
    open val timestamp: Long,
    open val tag: String? = null,
    open val message: String? = null,
    open val stackTrace: String? = null
) {

    fun asString(): String =
        "{ " +
            "\"level\": \"$level\", " +
            "\"timestamp\": $timestamp, " +
            "\"tag\": ${tag?.let { "\"$it\"" } ?: "null"}, " +
            "\"message\": ${message?.let { "\"$it\"" } ?: "null"}, " +
            "\"stackTrace\": ${stackTrace?.let { "\"$it\"" } ?: "null"}" +
            " }"
}