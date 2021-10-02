package com.infinum.sentinel.ui.main.preferences

import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class PreferencesViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<PreferencesState, Nothing>() {

    override fun data() =
        launch {
            val result = io {
                collectors.preferences()()
            }
            setState(
                PreferencesState.Data(
                    value = result
                )
            )
        }
}
