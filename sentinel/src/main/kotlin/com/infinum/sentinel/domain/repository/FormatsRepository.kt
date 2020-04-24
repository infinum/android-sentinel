package com.infinum.sentinel.domain.repository

import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.sources.local.room.SentinelDatabase
import com.infinum.sentinel.data.sources.local.room.dao.FormatsDao
import java.util.concurrent.ExecutorService

internal class FormatsRepository(
    database: SentinelDatabase,
    private val executor: ExecutorService
) {
    private val formats: FormatsDao = database.formatsDao()

    fun save(entity: List<FormatEntity>) =
        executor.execute {
            formats.save(entity)
        }

    fun load() = formats.load()
}
