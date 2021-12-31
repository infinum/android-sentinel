package com.infinum.sentinel.ui.main.tools

import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class ToolsViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<ToolsState, Nothing>() {

    override fun data() =
        launch {
            val result = io {
                collectors.tools()()
            }
            setState(
                ToolsState.Data(
                    value = result
                )
            )
        }
}
