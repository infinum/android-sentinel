package com.infinum.sentinel.data.models.raw

internal data class PreferencesData(
    val name: String,
    val values: List<Triple<Class<out Any>, String, Any>>
)
