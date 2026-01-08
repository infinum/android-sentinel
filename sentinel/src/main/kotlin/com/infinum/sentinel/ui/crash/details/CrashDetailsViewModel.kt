package com.infinum.sentinel.ui.crash.details

import com.infinum.sentinel.data.sources.local.room.dao.CrashesDao
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.crash.models.CrashParameters
import com.infinum.sentinel.domain.crash.monitor.models.CrashMonitorParameters
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import com.infinum.sentinel.extensions.formatter
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@Inject
internal class CrashDetailsViewModel(
    private val crashMonitor: Repositories.CrashMonitor,
    private val dao: CrashesDao,
    private val formatters: Factories.Formatter,
    private val formats: Repositories.Formats,
) : BaseChildViewModel<CrashDetailsState, CrashDetailsEvent>() {
    private var parameters: CrashParameters = CrashParameters()

    override fun data() =
        launch {
            val result =
                io {
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
            val result =
                io {
                    val monitor = crashMonitor.load(CrashMonitorParameters()).firstOrNull()
                    val entity = dao.loadById(parameters.crashId!!)
                    val formatter =
                        formats
                            .load(FormatsParameters())
                            .map { it.type?.formatter(formatters) }
                            .firstOrNull()

                    formatter?.formatCrash(monitor?.includeAllData ?: false, entity).orEmpty()
                }
            emitEvent(CrashDetailsEvent.Formatted(result))
        }
    }
}
