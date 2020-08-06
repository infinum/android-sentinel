package com.infinum.sentinel.domain.repository

import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.sources.local.DatabaseProvider
import com.infinum.sentinel.data.sources.local.room.dao.FormatsDao

internal class FormatsRepository(
    databaseProvider: DatabaseProvider
) {

    private val formats: FormatsDao = databaseProvider.sentinel().formatsDao()

    suspend fun save(entity: List<FormatEntity>) = formats.save(entity)

    suspend fun load() = formats.load()
}
