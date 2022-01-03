package com.infinum.sentinel.ui.settings

import com.infinum.sentinel.data.models.local.BundleMonitorEntity
import com.infinum.sentinel.data.models.local.CrashMonitorEntity
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import com.infinum.sentinel.domain.crash.monitor.models.CrashMonitorParameters
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import com.infinum.sentinel.domain.triggers.models.TriggerParameters
import com.infinum.sentinel.ui.crash.anr.SentinelAnrObserver
import com.infinum.sentinel.ui.crash.handler.SentinelExceptionHandler
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn

internal class SettingsViewModel(
    private val triggers: Repositories.Triggers,
    private val formats: Repositories.Formats,
    private val bundleMonitor: Repositories.BundleMonitor,
    private val crashMonitor: Repositories.CrashMonitor,
    private val exceptionHandler: SentinelExceptionHandler,
    private val anrObserver: SentinelAnrObserver,
) : BaseChildViewModel<Nothing, SettingsEvent>() {

    override fun data() {
        launch {
            triggers.load(TriggerParameters())
                .flowOn(runningDispatchers)
                .collectLatest {
                    emitEvent(SettingsEvent.TriggersChanged(value = it))
                }
        }
        launch {
            formats.load(FormatsParameters())
                .flowOn(runningDispatchers)
                .collectLatest {
                    emitEvent(SettingsEvent.FormatChanged(value = it))
                }
        }
        launch {
            bundleMonitor.load(BundleMonitorParameters())
                .flowOn(runningDispatchers)
                .collectLatest {
                    emitEvent(SettingsEvent.BundleMonitorChanged(value = it))
                }
        }
        launch {
            crashMonitor.load(CrashMonitorParameters())
                .flowOn(runningDispatchers)
                .collectLatest {
                    emitEvent(SettingsEvent.CrashMonitorChanged(value = it))
                }
        }
    }

    fun toggleTrigger(entity: TriggerEntity) =
        launch {
            io {
                triggers.save(
                    TriggerParameters(
                        entity = entity
                    )
                )
            }
        }

    fun saveFormats(entities: List<FormatEntity>) =
        launch {
            io {
                formats.save(
                    FormatsParameters(
                        entities = entities
                    )
                )
            }
        }

    fun updateBundleMonitor(entity: BundleMonitorEntity) =
        launch {
            io {
                bundleMonitor.save(
                    BundleMonitorParameters(
                        entity = entity
                    )
                )
            }
        }

    fun updateCrashMonitor(entity: CrashMonitorEntity) =
        launch {
            io {
                crashMonitor.save(
                    CrashMonitorParameters(
                        entity = entity
                    )
                )
                if (entity.notifyExceptions) {
                    exceptionHandler.start()
                } else {
                    exceptionHandler.stop()
                }
                if (entity.notifyAnrs) {
                    anrObserver.start()
                } else {
                    anrObserver.stop()
                }
            }
        }
}
