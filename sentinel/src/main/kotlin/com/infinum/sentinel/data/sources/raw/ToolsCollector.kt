package com.infinum.sentinel.data.sources.raw

import com.infinum.sentinel.Sentinel

internal class ToolsCollector(
    private val tools: Set<Sentinel.Tool>
) : AbstractCollector<Set<Sentinel.Tool>>() {

    override lateinit var data: Set<Sentinel.Tool>

    override fun collect() {
        data = tools
    }
}