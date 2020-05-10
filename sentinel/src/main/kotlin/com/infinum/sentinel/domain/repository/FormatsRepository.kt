package com.infinum.sentinel.domain.repository

import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.sources.local.DatabaseProvider
import com.infinum.sentinel.data.sources.local.room.dao.FormatsDao
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class FormatsRepository(
    databaseProvider: DatabaseProvider
) {

    private val formats: FormatsDao = databaseProvider.sentinel().formatsDao()

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun save(entity: List<FormatEntity>) =
        executor.execute {
            formats.save(entity)
        }

    fun load() = formats.load()
}
