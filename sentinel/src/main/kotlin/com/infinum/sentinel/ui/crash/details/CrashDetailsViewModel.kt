package com.infinum.sentinel.ui.crash.details

import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.sources.local.room.dao.CrashesDao
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.crash.models.CrashParameters
import com.infinum.sentinel.domain.crash.monitor.models.CrashMonitorParameters
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.firstOrNull

internal class CrashDetailsViewModel(
    private val crashMonitor: Repositories.CrashMonitor,
    private val dao: CrashesDao,
    private val formatters: Factories.Formatter,
    private val formats: Repositories.Formats
) : BaseChildViewModel<CrashDetailsState, CrashDetailsEvent>() {

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

    fun remove() {
        launch {
            io {
                dao.deleteById(parameters.crashId!!)
            }
            emitEvent(CrashDetailsEvent.Removed())
        }
    }

    fun share() {
        launch {
            val result = io {
                val monitor = crashMonitor.load(CrashMonitorParameters()).firstOrNull()
                val entity = dao.loadById(parameters.crashId!!)
                val formatter = formats.load(FormatsParameters()).firstOrNull()
                when (formatter?.type) {
                    FormatType.PLAIN -> formatters.plain()
                    FormatType.MARKDOWN -> formatters.markdown()
                    FormatType.JSON -> formatters.json()
                    FormatType.XML -> formatters.xml()
                    FormatType.HTML -> formatters.html()
                    else -> null
                }
                    ?.formatCrash(monitor?.includeAllData ?: false, entity)
                    .orEmpty()
            }
            emitEvent(CrashDetailsEvent.Formatted(result))
        }
    }
}
