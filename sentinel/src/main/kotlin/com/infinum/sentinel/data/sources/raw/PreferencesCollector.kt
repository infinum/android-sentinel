package com.infinum.sentinel.data.sources.raw

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.annotation.VisibleForTesting
import com.infinum.sentinel.data.models.raw.PreferencesData
import java.io.File

internal class PreferencesCollector(
    private val context: Context
) : AbstractCollector<List<PreferencesData>>() {

    companion object {
        @VisibleForTesting
        const val PREFS_DIRECTORY = "shared_prefs"

        @VisibleForTesting
        const val PREFS_SUFFIX = ".xml"
    }

    override lateinit var data: List<PreferencesData>

    override fun collect() {
        data = with(context) {
            val prefsDirectory = File(applicationContext.applicationInfo.dataDir, PREFS_DIRECTORY)
            if (prefsDirectory.exists() && prefsDirectory.isDirectory) {
                prefsDirectory.list().orEmpty().toList().map { it.removeSuffix(PREFS_SUFFIX) }
            } else {
                listOf()
            }.map { name ->
                val allPrefs = getSharedPreferences(name, MODE_PRIVATE).all
                val tuples = allPrefs.keys.toSet().mapNotNull {
                    @Suppress("UNCHECKED_CAST")
                    when (val value = allPrefs[it]) {
                        is Boolean -> Triple(Boolean::class.java, it, value)
                        is Float -> Triple(Float::class.java, it, value)
                        is Int -> Triple(Int::class.java, it, value)
                        is Long -> Triple(Long::class.java, it, value)
                        is String -> Triple(String::class.java, it, value)
                        is Set<*> -> Triple(Set::class.java, it, value as Set<String>)
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
}
