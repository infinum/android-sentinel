package com.infinum.sentinel.ui.settings

import com.infinum.sentinel.data.models.local.BundleMonitorEntity
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import com.infinum.sentinel.domain.triggers.models.TriggerParameters
import com.infinum.sentinel.ui.crash.anr.AnrObserver
import com.infinum.sentinel.ui.crash.handler.SentinelExceptionHandler
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn

internal class SettingsViewModel(
    private val triggers: Repositories.Triggers,
    private val formats: Repositories.Formats,
    private val bundleMonitor: Repositories.BundleMonitor,
    private val exceptionHandler: SentinelExceptionHandler,
    private val anrObserver: AnrObserver
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

    fun toggleUncaughtException(value: Boolean) =
        launch {
            io {
                if (value) {
                    exceptionHandler.startCatchingUncaughtExceptions()
                } else {
                    exceptionHandler.stopCatchingUncaughtExceptions()
                }
            }
        }

    fun toggleAnrException(value: Boolean) =
        launch {
            io {
                if (value) {
                    anrObserver.start()
                } else {
                    anrObserver.stop()
                }
            }
        }
}
