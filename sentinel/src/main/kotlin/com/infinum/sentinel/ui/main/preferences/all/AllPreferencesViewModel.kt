package com.infinum.sentinel.ui.main.preferences.all

import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.preference.models.PreferenceParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import me.tatarka.inject.annotations.Inject

@Inject
internal class AllPreferencesViewModel(
    private val collectors: Factories.Collector,
    private val repository: Repositories.Preference
) : BaseChildViewModel<AllPreferencesState, AllPreferencesEvent>() {

    override fun data() =
        launch {
            val result = io {
                collectors.preferences()()
            }
            setState(
                AllPreferencesState.Data(
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
            emitEvent(AllPreferencesEvent.Cached())
        }

    fun onSortClicked(prefParentName: String) {
        val currentValues = (stateFlow.value as? AllPreferencesState.Data)?.value.orEmpty()

        val changedValues = currentValues.map { preferencesData ->
            if (preferencesData.name == prefParentName) {
                val sortedData = if (preferencesData.isSortedAscending) {
                    preferencesData.values.sortedByDescending { it.second }
                } else {
                    preferencesData.values.sortedBy { it.second }
                }
                preferencesData.copy(
                    values = sortedData,
                    isSortedAscending = !preferencesData.isSortedAscending
                )
            } else {
                preferencesData
            }
        }

        setState(AllPreferencesState.Data(value = changedValues))
    }

    fun onHideExpandClicked(prefParentName: String) {
        val currentValues = (stateFlow.value as? AllPreferencesState.Data)?.value.orEmpty()

        val changedValues = currentValues.map { preferencesData ->
            if (preferencesData.name == prefParentName) {
                preferencesData.copy(isExpanded = !preferencesData.isExpanded)
            } else {
                preferencesData
            }
        }

        setState(AllPreferencesState.Data(value = changedValues))
    }
}
