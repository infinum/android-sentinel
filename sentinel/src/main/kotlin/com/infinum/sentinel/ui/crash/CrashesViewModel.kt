package com.infinum.sentinel.ui.crash

import androidx.lifecycle.viewModelScope
import com.infinum.sentinel.data.sources.local.room.dao.CrashesDao
import com.infinum.sentinel.domain.crash.models.CrashParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject

@Inject
internal class CrashesViewModel(
    private val dao: CrashesDao,
) : BaseChildViewModel<Nothing, CrashesEvent>() {
    private var parameters: CrashParameters = CrashParameters()

    override fun data() {
        dao
            .loadAll()
            .map {
                if (parameters.query?.lowercase().isNullOrBlank()) {
                    it
                } else {
                    it.filter { entity ->
                        entity.data.exception?.name?.lowercase()?.contains(
                            parameters.query?.lowercase().orEmpty(),
                        ) ?: true ||
                            entity.data.exception?.lineNumber?.toString()?.lowercase()?.contains(
                                parameters.query?.lowercase().orEmpty(),
                            ) ?: true ||
                            entity.data.exception?.file?.lowercase()?.contains(
                                parameters.query?.lowercase().orEmpty(),
                            ) ?: true ||
                            entity.data.exception?.message?.lowercase()?.contains(
                                parameters.query?.lowercase().orEmpty(),
                            ) ?: true
                    }
                }
            }.flowOn(runningDispatchers)
            .onEach { emitEvent(CrashesEvent.CrashesIntercepted(value = it)) }
            .launchIn(viewModelScope)
    }

    fun clearCrashes() =
        launch {
            io {
                dao.deleteAll()
            }
        }

    fun setSearchQuery(query: String?) {
        parameters = parameters.copy(query = query)
        data()
    }
}
