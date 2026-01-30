package com.infinum.sentinel.ui.logger.storage

internal object AllowedTags {
    private var tags: List<String>? = null

    val value: List<String>
        get() = tags ?: emptyList()

    fun set(allowedTags: List<String>) {
        tags = allowedTags
    }
}
