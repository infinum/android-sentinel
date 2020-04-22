package com.infinum.sentinel.data.models.memory.storage

data class StoredPreferences(
    val name: String,
    val values: List<Triple<Class<out Any>, String, Any>>
)
