package com.infinum.sentinel.domain.preference

import android.annotation.SuppressLint
import android.content.Context
import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.data.sources.memory.preference.PreferenceCache
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.preference.models.PreferenceParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class PreferenceRepository(
    private val context: Context,
    private val memoryCache: PreferenceCache
) : Repositories.Preference {

    override fun cache(cache: PreferenceParameters.Cache) {
        memoryCache.save(cache.name, cache.value)
    }

    override fun consume(): Pair<String, Triple<PreferenceType, String, Any>> {
        val item = memoryCache.load()
        memoryCache.clear()
        return item
    }

    @SuppressLint("ApplySharedPref")
    override suspend fun save(input: PreferenceParameters) {
        val editor = context
            .getSharedPreferences(input.name, Context.MODE_PRIVATE)
            .takeIf { it.contains(input.key) }
            ?.edit()

        when (input) {
            is PreferenceParameters.BooleanType -> editor?.putBoolean(input.key, input.value)?.commit()
            is PreferenceParameters.FloatType -> editor?.putFloat(input.key, input.value)?.commit()
            is PreferenceParameters.IntType -> editor?.putInt(input.key, input.value)?.commit()
            is PreferenceParameters.LongType -> editor?.putLong(input.key, input.value)?.commit()
            is PreferenceParameters.StringType -> editor?.putString(input.key, input.value)?.commit()
            is PreferenceParameters.ArrayType -> editor?.putStringSet(input.key, input.value.toSet())?.commit()
            else -> Unit
        }
    }

    override suspend fun load(input: PreferenceParameters): Flow<Unit> = flowOf(Unit)
}
