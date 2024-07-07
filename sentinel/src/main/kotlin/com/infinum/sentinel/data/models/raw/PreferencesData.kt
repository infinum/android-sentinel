package com.infinum.sentinel.data.models.raw

internal data class PreferencesData(
    val name: String,
    val values: List<Triple<PreferenceType, String, Any>>,
    val isSortedAscending: Boolean = false,
)
