package com.infinum.sentinel.domain.shared.base

import kotlinx.coroutines.flow.Flow

internal interface BaseRepository<InputModel : BaseParameters, OutputModel> {

    suspend fun save(input: InputModel): Unit = throw NotImplementedError()

    suspend fun load(input: InputModel): Flow<OutputModel> = throw NotImplementedError()
}
