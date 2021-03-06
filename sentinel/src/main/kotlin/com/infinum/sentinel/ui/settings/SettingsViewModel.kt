package com.infinum.sentinel.ui.settings

import com.infinum.sentinel.data.models.local.BundleMonitorEntity
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundlemonitor.models.BundleMonitorParameters
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import com.infinum.sentinel.domain.triggers.models.TriggerParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn

internal class SettingsViewModel(
    private val triggers: Repositories.Triggers,
    private val formats: Repositories.Formats,
    private val bundleMonitor: Repositories.BundleMonitor
) : BaseChildViewModel<Any>() {

    override fun data(action: (Any) -> Unit) = throw NotImplementedError()

    fun triggers(action: (List<TriggerEntity>) -> Unit) =
        launch {
            triggers.load(TriggerParameters())
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    action(it)
                }
        }

    fun formats(action: (FormatEntity) -> Unit) =
        launch {
            formats.load(FormatsParameters())
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    action(it)
                }
        }

    fun bundleMonitor(action: (BundleMonitorEntity) -> Unit) =
        launch {
            bundleMonitor.load(BundleMonitorParameters())
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    action(it)
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

    fun toggleBundleMonitorNotify(entity: BundleMonitorEntity) =
        launch {
            io {
                bundleMonitor.save(
                    BundleMonitorParameters(
                        entity = entity
                    )
                )
            }
        }

    fun saveBundleMonitorLimit(entity: BundleMonitorEntity) =
        launch {
            io {
                bundleMonitor.save(
                    BundleMonitorParameters(
                        entity = entity
                    )
                )
            }
        }
}
