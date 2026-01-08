package com.infinum.sentinel.domain.shared.base

import kotlinx.coroutines.flow.Flow

@Suppress("NotImplementedDeclaration")
internal interface BaseRepository<InputModel : BaseParameters, OutputModel> {
    suspend fun save(input: InputModel): Unit = throw NotImplementedError()

    fun load(input: InputModel): Flow<OutputModel> = throw NotImplementedError()
}
