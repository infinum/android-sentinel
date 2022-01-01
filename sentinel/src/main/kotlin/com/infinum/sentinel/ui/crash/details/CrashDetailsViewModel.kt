package com.infinum.sentinel.ui.crash.details

import com.infinum.sentinel.data.sources.local.room.dao.CrashesDao
import com.infinum.sentinel.domain.crash.models.CrashParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class CrashDetailsViewModel(
    private val dao: CrashesDao
) : BaseChildViewModel<CrashDetailsState, Nothing>() {

    private var parameters: CrashParameters = CrashParameters()

    override fun data() =
        launch {
            val result = io {
                dao.loadById(parameters.crashId!!)
            }
            setState(CrashDetailsState.Data(value = result))
        }

    fun setCrashId(value: Long) {
        parameters = parameters.copy(crashId = value)
    }
}
