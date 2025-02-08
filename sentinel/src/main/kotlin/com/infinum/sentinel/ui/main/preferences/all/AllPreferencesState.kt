package com.infinum.sentinel.ui.main.preferences.all

import com.infinum.sentinel.data.models.raw.PreferencesData

internal sealed class AllPreferencesState {

    data class Data(
        val value: List<PreferencesData>
    ) : AllPreferencesState()
}
