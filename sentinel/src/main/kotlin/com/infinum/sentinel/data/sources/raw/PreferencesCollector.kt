package com.infinum.sentinel.data.sources.raw

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.infinum.sentinel.data.models.raw.PreferencesData
import java.io.File

internal class PreferencesCollector(
    private val context: Context
) : AbstractCollector<List<PreferencesData>>() {

    override lateinit var data: List<PreferencesData>

    override fun collect() {
        data = with(context) {
            val prefsDirectory = File(
                    applicationContext.applicationInfo.dataDir,
                    "shared_prefs"
                )
            if (prefsDirectory.exists() && prefsDirectory.isDirectory) {
                prefsDirectory.list().orEmpty().toList().map { it.removeSuffix(".xml") }
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
