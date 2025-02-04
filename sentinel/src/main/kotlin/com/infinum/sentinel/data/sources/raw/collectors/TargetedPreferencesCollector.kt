package com.infinum.sentinel.data.sources.raw.collectors

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.domain.collectors.Collectors
import me.tatarka.inject.annotations.Inject

@Inject
internal class TargetedPreferencesCollector(
    private val context: Context,
    private val targetedPreferences: Map<String, List<String>>
) : Collectors.TargetedPreferences {

    override fun invoke(): List<PreferencesData> {
        if (targetedPreferences.isEmpty()) return emptyList()

        return targetedPreferences.mapNotNull { (fileName, keys) ->
            val allPrefs = context.getSharedPreferences(fileName, MODE_PRIVATE).all

            val tuples = allPrefs.keys
                .filter { keys.isEmpty() || keys.contains(it) }
                .mapNotNull { key ->
                    @Suppress("UNCHECKED_CAST")
                    when (val value = allPrefs[key]) {
                        is Boolean -> Triple(PreferenceType.BOOLEAN, key, value)
                        is Float -> Triple(PreferenceType.FLOAT, key, value)
                        is Int -> Triple(PreferenceType.INT, key, value)
                        is Long -> Triple(PreferenceType.LONG, key, value)
                        is String -> Triple(PreferenceType.STRING, key, value)
                        is Set<*> -> Triple(PreferenceType.SET, key, value as Set<String>)
                        else -> null
                    }
                }

            if (tuples.isNotEmpty()) PreferencesData(fileName, tuples) else null
        }
    }
}