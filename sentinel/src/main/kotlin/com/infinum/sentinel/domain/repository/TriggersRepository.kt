package com.infinum.sentinel.domain.repository

import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.data.models.memory.CacheProvider
import com.infinum.sentinel.data.models.memory.triggers.TriggerType
import com.infinum.sentinel.data.models.raw.DeviceData
import com.infinum.sentinel.data.sources.local.DatabaseProvider
import com.infinum.sentinel.data.sources.local.room.dao.TriggersDao
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class TriggersRepository(
    databaseProvider: DatabaseProvider,
    cacheProvider: CacheProvider
) {

    private val triggersLocal: TriggersDao = databaseProvider.sentinel().triggersDao()

    private val triggersMemory = cacheProvider.triggers()

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        observe()
        if (DeviceData().isProbablyAnEmulator) {
            foreground()
        }
    }

    fun save(entity: TriggerEntity) =
        executor.execute {
            triggersLocal.save(entity)
        }

    fun load() = triggersLocal.load()

    private fun foreground() =
        executor.execute {
            save(triggersLocal.foreground().apply {
                this.editable = false
                this.enabled = true
            })
        }

    private fun observe() {
        load().observeForever { triggers ->
            triggers.forEach {
                when (it.type) {
                    TriggerType.MANUAL -> triggersMemory.manual().active = it.enabled
                    TriggerType.FOREGROUND -> triggersMemory.foreground().active = it.enabled
                    TriggerType.SHAKE -> triggersMemory.shake().active = it.enabled
                    TriggerType.USB_CONNECTED -> triggersMemory.usbConnected().active = it.enabled
                    TriggerType.AIRPLANE_MODE_ON -> triggersMemory.airplaneModeOn().active =
                        it.enabled
                }
            }
        }
    }
}
