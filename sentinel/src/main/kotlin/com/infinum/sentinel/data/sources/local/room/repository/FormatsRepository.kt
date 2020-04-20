package com.infinum.sentinel.data.sources.local.room.repository

import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.sources.local.room.SentinelDatabase
import com.infinum.sentinel.data.sources.local.room.dao.FormatsDao
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal object FormatsRepository {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    private lateinit var formats: FormatsDao

    fun initialize(database: SentinelDatabase) {
        formats = database.formatsDao()
    }

    fun initValues() {
        save(
            listOf(
                FormatEntity(
                    id = FormatType.PLAIN.ordinal.toLong(),
                    type = FormatType.PLAIN,
                    selected = true
                ),
                FormatEntity(
                    id = FormatType.MARKDOWN.ordinal.toLong(),
                    type = FormatType.MARKDOWN,
                    selected = false
                ),
                FormatEntity(
                    id = FormatType.JSON.ordinal.toLong(),
                    type = FormatType.JSON,
                    selected = false
                ),
                FormatEntity(
                    id = FormatType.XML.ordinal.toLong(),
                    type = FormatType.XML,
                    selected = false
                ),
                FormatEntity(
                    id = FormatType.HTML.ordinal.toLong(),
                    type = FormatType.HTML,
                    selected = false
                )
            )
        )
    }

    fun save(entity: List<FormatEntity>) =
        executor.execute {
            formats.save(entity)
        }

    fun load() = formats.load()
}
