package com.infinum.sentinel.data.sources.raw.collectors

import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.ui.tools.AppInfoTool

internal class ToolsCollector(
    private val tools: Set<Sentinel.Tool>
) : Collectors.Tools {

    override fun invoke() =
        tools.plus(AppInfoTool()).filterNot { it.name() == 0 }.toSet()
}
