package com.infinum.sentinel.data.sources.local.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import me.tatarka.inject.annotations.Inject

/**
 * Keeps deep link variants in the `sentinel_deep_links` preferences file.
 */
@Inject
internal class SharedPreferencesDeepLinksStore(
    private val context: Context,
) : DeepLinksStore {
    companion object {
        private const val FILE_NAME = "sentinel_deep_links"
        private const val KEY_VARIANTS = "variants"
        private const val KEY_VARIANTS_CORRUPT_BACKUP = "variants_corrupt_backup"
    }

    // Lazy, because dependency injection builds objects eagerly and this must not touch disk then.
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    override fun readVariants(): String? = preferences.getString(KEY_VARIANTS, null)

    override fun writeVariants(json: String): Boolean = write(KEY_VARIANTS, json)

    override fun backupCorrupt(json: String): Boolean = write(KEY_VARIANTS_CORRUPT_BACKUP, json)

    /**
     * Writes [value] and returns whether it reached disk. SharedPreferences updates its in-memory value even
     * when the commit fails, so a failed commit puts the previous value back in memory, and later reads
     * don't report data that was never persisted.
     */
    // The KTX edit(commit = true) {} returns Unit; callers need commit's result to know the write reached disk.
    // The revert uses apply(): the in-memory value is restored at once and there is nothing to report from it.
    @SuppressLint("UseKtx")
    private fun write(
        key: String,
        value: String,
    ): Boolean {
        val previous = preferences.getString(key, null)
        val isCommitted = preferences.edit().putString(key, value).commit()
        if (!isCommitted) {
            val editor = preferences.edit()
            if (previous == null) editor.remove(key) else editor.putString(key, previous)
            editor.apply()
        }
        return isCommitted
    }
}
