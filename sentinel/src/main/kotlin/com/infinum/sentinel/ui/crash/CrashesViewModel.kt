package com.infinum.sentinel.ui.crash

import androidx.lifecycle.viewModelScope
import com.infinum.sentinel.data.sources.local.room.dao.CrashesDao
import com.infinum.sentinel.domain.crash.models.CrashParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class CrashesViewModel(
    private val dao: CrashesDao
) : BaseChildViewModel<Nothing, CrashesEvent>() {

    private var parameters: CrashParameters = CrashParameters()

    override fun data() {
        dao.loadAll()
            .flowOn(runningDispatchers)
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
