package com.infinum.sentinel.data.sources.memory.targetedpreferences

import com.infinum.sentinel.data.models.raw.PreferenceType

internal interface TargetedPreferencesCache {

    fun save(name: String, tuple: Triple<PreferenceType, String, Any>)

    fun load(): Pair<String, Triple<PreferenceType, String, Any>>

    fun clear()
}