package com.infinum.sentinel.data.sources.raw

import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.ui.tools.AppInfoTool

internal class ToolsCollector(
    private val tools: Set<Sentinel.Tool>
) : Collector<Set<Sentinel.Tool>> {

    override fun invoke() =
        tools.plus(AppInfoTool()).filterNot { it.name() == 0 }.toSet()
}
