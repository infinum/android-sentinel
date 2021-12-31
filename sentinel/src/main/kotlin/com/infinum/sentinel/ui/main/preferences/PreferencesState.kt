package com.infinum.sentinel.ui.main.preferences

import com.infinum.sentinel.data.models.raw.PreferencesData

internal sealed class PreferencesState {

    data class Data(
        val value: List<PreferencesData>
    ) : PreferencesState()
}
