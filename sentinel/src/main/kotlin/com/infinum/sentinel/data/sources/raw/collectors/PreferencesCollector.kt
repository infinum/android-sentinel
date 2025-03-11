package com.infinum.sentinel.data.sources.raw.collectors

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.annotation.VisibleForTesting
import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.domain.collectors.Collectors
import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
internal class PreferencesCollector(
    private val context: Context,
    private val targetedPreferences: Map<String, List<String>>,
) : Collectors.Preferences {

    companion object {
        @VisibleForTesting
        const val PREFS_DIRECTORY = "shared_prefs"

        @VisibleForTesting
        const val PREFS_SUFFIX = ".xml"
    }

    override fun invoke(filter: Boolean): List<PreferencesData> {
        val prefsDirectory = File(context.applicationContext.applicationInfo.dataDir, PREFS_DIRECTORY)

        if (!prefsDirectory.exists() || !prefsDirectory.isDirectory) {
            return emptyList()
        }

        val preferenceFiles = if (filter) {
            targetedPreferences.keys
        } else {
            prefsDirectory.list().orEmpty().map { it.removeSuffix(PREFS_SUFFIX) }
        }

        return preferenceFiles.sorted()
            .mapNotNull { name ->
                val allPrefs = context.getSharedPreferences(name, MODE_PRIVATE).all

                val filteredKeys = if (filter) targetedPreferences[name].orEmpty().toSet() else null

                val tuples = allPrefs.keys
                    .filter { filteredKeys.isNullOrEmpty() || filteredKeys.contains(it) }
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

                if (tuples.isNotEmpty()) PreferencesData(name, tuples) else null
            }
    }

    override fun invoke(): List<PreferencesData> = invoke(filter = false)
}
