package com.infinum.sentinel.data.sources.local.room.repository

import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.data.models.memory.triggers.TriggerType
import com.infinum.sentinel.data.sources.local.room.SentinelDatabase
import com.infinum.sentinel.data.sources.local.room.dao.TriggersDao
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal object TriggersRepository {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    private lateinit var triggers: TriggersDao

    fun initialize(database: SentinelDatabase) {
        triggers = database.triggersDao()
    }

    fun initValues() {
        save(
            TriggerEntity(
                id = TriggerType.MANUAL.ordinal.toLong(),
                type = TriggerType.MANUAL,
                enabled = true,
                editable = false
            )
        )
        save(
            TriggerEntity(
                id = TriggerType.SHAKE.ordinal.toLong(),
                type = TriggerType.SHAKE,
                enabled = true,
                editable = false
            )
        )
        save(
            TriggerEntity(
                id = TriggerType.USB_CONNECTED.ordinal.toLong(),
                type = TriggerType.USB_CONNECTED,
                enabled = true,
                editable = true
            )
        )
        save(
            TriggerEntity(
                id = TriggerType.FOREGROUND.ordinal.toLong(),
                type = TriggerType.FOREGROUND,
                enabled = true,
                editable = true
            )
        )
        save(
            TriggerEntity(
                id = TriggerType.AIRPLANE_MODE_ON.ordinal.toLong(),
                type = TriggerType.AIRPLANE_MODE_ON,
                enabled = true,
                editable = true
            )
        )
    }

    fun save(entity: TriggerEntity) =
        executor.execute {
            triggers.save(entity)
        }

    fun load() = triggers.load()
}
