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
    private val context: Context
) : Collectors.Preferences {

    companion object {
        @VisibleForTesting
        const val PREFS_DIRECTORY = "shared_prefs"

        @VisibleForTesting
        const val PREFS_SUFFIX = ".xml"
    }

    override fun invoke() =
        with(context) {
            val prefsDirectory = File(applicationContext.applicationInfo.dataDir, PREFS_DIRECTORY)
            if (prefsDirectory.exists() && prefsDirectory.isDirectory) {
                prefsDirectory.list().orEmpty().toList().map { it.removeSuffix(PREFS_SUFFIX) }
            } else {
                listOf()
            }.sortedBy { name ->
                name
            }.map { name ->
                val allPrefs = getSharedPreferences(name, MODE_PRIVATE).all
                val tuples = allPrefs.keys.toSet().mapNotNull {
                    @Suppress("UNCHECKED_CAST")
                    when (val value = allPrefs[it]) {
                        is Boolean -> Triple(PreferenceType.BOOLEAN, it, value)
                        is Float -> Triple(PreferenceType.FLOAT, it, value)
                        is Int -> Triple(PreferenceType.INT, it, value)
                        is Long -> Triple(PreferenceType.LONG, it, value)
                        is String -> Triple(PreferenceType.STRING, it, value)
                        is Set<*> -> Triple(PreferenceType.SET, it, value as Set<String>)
                        else -> null
                    }
                }
                PreferencesData(
                    name = name,
                    values = tuples
                )
            }
        }
}
