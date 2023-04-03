package com.infinum.sentinel.data.sources.raw.collectors

import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.domain.collectors.Collectors
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
internal class ToolsCollector(
    @Assisted private val tools: Set<Sentinel.Tool>
) : Collectors.Tools {

    override fun invoke() =
        tools.filterNot { it.name() == 0 }.toSet()
}
