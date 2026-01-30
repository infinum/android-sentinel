package com.infinum.sentinel.domain.triggers

import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.data.models.memory.triggers.TriggerType
import com.infinum.sentinel.data.sources.local.room.dao.TriggersDao
import com.infinum.sentinel.data.sources.memory.triggers.TriggersCache
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.triggers.models.TriggerParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject

@Inject
internal class TriggersRepository(
    private val dao: TriggersDao,
    private val cache: TriggersCache,
) : Repositories.Triggers {
    override suspend fun save(input: TriggerParameters) =
        input.entity?.let {
            dao.save(it)
            updateCache(it)
        } ?: error("Cannot save null entity")

    override fun load(input: TriggerParameters): Flow<List<TriggerEntity>> =
        dao
            .load()
            .onEach { it.forEach { entity -> updateCache(entity) } }

    private fun updateCache(entity: TriggerEntity) =
        when (entity.type) {
            TriggerType.MANUAL -> cache.manual().active = entity.enabled
            TriggerType.FOREGROUND -> cache.foreground().active = entity.enabled
            TriggerType.SHAKE -> cache.shake().active = entity.enabled
            TriggerType.PROXIMITY -> cache.proximity().active = entity.enabled
            TriggerType.USB_CONNECTED -> cache.usbConnected().active = entity.enabled
            TriggerType.AIRPLANE_MODE_ON -> cache.airplaneModeOn().active = entity.enabled
            else -> Unit
        }
}
