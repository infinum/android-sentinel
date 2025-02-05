package com.infinum.sentinel.ui.main.preferences.targeted

import com.infinum.sentinel.data.models.raw.PreferencesData

internal sealed class TargetedPreferencesState {

    data class Data(
        val value: List<PreferencesData>
    ) : TargetedPreferencesState()
}
