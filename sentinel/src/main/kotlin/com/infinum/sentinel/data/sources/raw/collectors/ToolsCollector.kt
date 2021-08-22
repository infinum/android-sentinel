package com.infinum.sentinel.data.sources.raw.collectors

import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.domain.collectors.Collectors

internal class ToolsCollector(
    private val tools: Set<Sentinel.Tool>
) : Collectors.Tools {

    override fun invoke() =
        tools.filterNot { it.name() == 0 }.toSet()
}
