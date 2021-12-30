package com.infinum.sentinel.domain.preference

import android.annotation.SuppressLint
import android.content.Context
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.preference.models.PreferenceParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class PreferenceRepository(
    private val context: Context
) : Repositories.Preference {

    @SuppressLint("ApplySharedPref")
    override suspend fun save(input: PreferenceParameters) {
        val editor = context
            .getSharedPreferences(input.name, Context.MODE_PRIVATE)
            .takeIf { it.contains(input.key) }
            ?.edit()

        when (input) {
            is PreferenceParameters.BooleanType -> editor?.putBoolean(input.key, input.value)
            is PreferenceParameters.FloatType -> editor?.putFloat(input.key, input.value)
            is PreferenceParameters.IntType -> editor?.putInt(input.key, input.value)
            is PreferenceParameters.LongType -> editor?.putLong(input.key, input.value)
            is PreferenceParameters.StringType -> editor?.putString(input.key, input.value)
            is PreferenceParameters.ArrayType -> editor?.putStringSet(input.key, input.value.toSet())
        }

        editor?.commit()
    }

    override suspend fun load(input: PreferenceParameters): Flow<Unit> = flowOf(Unit)
}
