package com.infinum.sentinel.ui.main.tools

import com.infinum.sentinel.Sentinel

internal sealed class ToolsState {

    data class Data(
        val value: Set<Sentinel.Tool>
    ) : ToolsState()
}
