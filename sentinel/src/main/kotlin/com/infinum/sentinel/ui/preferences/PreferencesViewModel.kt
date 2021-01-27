package com.infinum.sentinel.ui.preferences

import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class PreferencesViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<List<PreferencesData>>() {

    override fun data(action: (List<PreferencesData>) -> Unit) =
        launch {
            val result = io {
                collectors.preferences()()
            }
            action(result)
        }
}