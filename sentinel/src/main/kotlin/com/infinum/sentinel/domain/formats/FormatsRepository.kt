package com.infinum.sentinel.domain.formats

import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.sources.local.room.dao.FormatsDao
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import kotlinx.coroutines.flow.Flow

internal class FormatsRepository(
    private val dao: FormatsDao
) : Repositories.Formats {

    override suspend fun save(input: FormatsParameters) {
        input.entities?.let { dao.save(it) }
            ?: throw IllegalStateException("Cannot save null entities")
    }

    override suspend fun load(input: FormatsParameters): Flow<FormatEntity> =
        dao.load()
}
