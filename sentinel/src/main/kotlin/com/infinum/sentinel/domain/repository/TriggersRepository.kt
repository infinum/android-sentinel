package com.infinum.sentinel.domain.repository

import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.data.sources.local.room.SentinelDatabase
import java.util.concurrent.ExecutorService

internal class TriggersRepository(
    database: SentinelDatabase,
    private val executor: ExecutorService
) {
    private val triggers = database.triggersDao()

    fun save(entity: TriggerEntity) =
        executor.execute {
            triggers.save(entity)
        }

    fun load() = triggers.load()
}
