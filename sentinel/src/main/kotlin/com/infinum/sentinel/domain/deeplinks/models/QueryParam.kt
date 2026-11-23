package com.infinum.sentinel.domain.deeplinks.models

/**
 * A single query parameter of a deep link. Both [key] and [value] are decoded plain text.
 */
internal data class QueryParam(
    val key: String,
    val value: String,
)
