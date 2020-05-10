package com.infinum.sentinel.domain

import android.content.Context
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.DataGraph
import com.infinum.sentinel.domain.repository.CollectorRepository
import com.infinum.sentinel.domain.repository.FormatsRepository
import com.infinum.sentinel.domain.repository.FormatterRepository
import com.infinum.sentinel.domain.repository.TriggersRepository

internal class DomainGraph(context: Context) {

    private val data = DataGraph(context)

    private val collectors = CollectorRepository(context)

    private val formatters = FormatterRepository(context)

    private lateinit var formats: FormatsRepository

    private lateinit var triggers: TriggersRepository

    fun setup(tools: Set<Sentinel.Tool>, onTriggered: () -> Unit) {
        data.setup(onTriggered)

        formats = FormatsRepository(data.database())

        triggers = TriggersRepository(data.database(), data.cache())

        collectors.setup(tools)
    }

    fun collectors() = collectors

    fun formatters() = formatters

    fun formats() = formats

    fun triggers() = triggers
}
