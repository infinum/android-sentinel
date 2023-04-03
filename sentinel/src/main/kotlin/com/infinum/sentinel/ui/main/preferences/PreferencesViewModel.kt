package com.infinum.sentinel.ui.main.preferences

import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.preference.models.PreferenceParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import me.tatarka.inject.annotations.Inject

@Inject
internal class PreferencesViewModel(
    private val collectors: Factories.Collector,
    private val repository: Repositories.Preference
) : BaseChildViewModel<PreferencesState, PreferencesEvent>() {

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

    fun cache(name: String, tuple: Triple<PreferenceType, String, Any>) =
        launch {
            io {
                repository.cache(
                    PreferenceParameters.Cache(
                        name = name,
                        key = tuple.second,
                        value = tuple
                    )
                )
            }
            emitEvent(PreferencesEvent.Cached())
        }
}
