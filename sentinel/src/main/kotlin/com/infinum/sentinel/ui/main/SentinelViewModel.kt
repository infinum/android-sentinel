package com.infinum.sentinel.ui.main

import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.models.memory.triggers.TriggerType
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import com.infinum.sentinel.domain.triggers.models.TriggerParameters
import com.infinum.sentinel.ui.shared.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn

internal class SentinelViewModel(
    private val collectors: Factories.Collector,
    private val formatters: Factories.Formatter,
    private val triggers: Repositories.Triggers,
    private val formats: Repositories.Formats
) : BaseViewModel<SentinelState, SentinelEvent>() {

    fun checkIfEmulator() {
        launch {
            io {
                if (collectors.device()().isProbablyAnEmulator) {
                    triggers.load(TriggerParameters())
                        .firstOrNull()
                        ?.firstOrNull { it.type == TriggerType.FOREGROUND }
                        ?.let {
                            triggers.save(
                                TriggerParameters(
                                    entity = it.copy(
                                        enabled = true,
                                        editable = false
                                    )
                                )
                            )
                        }
                }
            }
        }
    }

    fun applicationIconAndName() {
        launch {
            val result = io {
                collectors.application()().let {
                    it.applicationIcon to it.applicationName
                }
            }
            setState(
                SentinelState.ApplicationIconAndName(
                    icon = result.first,
                    name = result.second
                )
            )
        }
    }

    fun formatData() =
        launch {
            formats.load(FormatsParameters())
                .flowOn(runningDispatchers)
                .collectLatest {
                    when (it.type) {
                        FormatType.PLAIN -> formatters.plain()
                        FormatType.MARKDOWN -> formatters.markdown()
                        FormatType.JSON -> formatters.json()
                        FormatType.XML -> formatters.xml()
                        FormatType.HTML -> formatters.html()
                        else -> null
                    }
                        ?.invoke()
                        ?.let { text ->
                            emitEvent(SentinelEvent.Formatted(text))
                        }
                }
        }
}
