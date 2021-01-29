package com.infinum.sentinel.ui

import android.graphics.drawable.Drawable
import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.models.memory.triggers.TriggerType
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import com.infinum.sentinel.domain.triggers.models.TriggerParameters
import com.infinum.sentinel.ui.shared.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn

internal class SentinelViewModel(
    private val collectors: Factories.Collector,
    private val formatters: Factories.Formatter,
    private val triggers: Repositories.Triggers,
    private val formats: Repositories.Formats
) : BaseViewModel() {

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

    fun applicationIconAndName(action: (Pair<Drawable, String>) -> Unit) {
        launch {
            val result = io {
                collectors.application()().let {
                    it.applicationIcon to it.applicationName
                }
            }
            action(result)
        }
    }

    fun formatData(action: (String) -> Unit) =
        launch {
            formats.load(FormatsParameters())
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    when (it.type) {
                        FormatType.PLAIN -> formatters.plain()
                        FormatType.MARKDOWN -> formatters.markdown()
                        FormatType.JSON -> formatters.json()
                        FormatType.XML -> formatters.xml()
                        FormatType.HTML -> formatters.html()
                        else -> null
                    }?.invoke()?.let(action)
                }
        }
}
