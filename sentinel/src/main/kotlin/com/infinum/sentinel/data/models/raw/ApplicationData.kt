package com.infinum.sentinel.data.models.raw

internal data class ApplicationData(
    val versionCode: String,
    val versionName: String,
    val firstInstall: String,
    val lastUpdate: String,
    val minSdk: String,
    val targetSdk: String,
    val packageName: String,
    val processName: String,
    val taskAffinity: String,
    val localeCountry: String,
    val localeLanguage: String
)