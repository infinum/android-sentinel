package com.infinum.sentinel.domain.crash.monitor

import com.infinum.sentinel.data.models.local.CrashMonitorEntity
import com.infinum.sentinel.data.sources.local.room.dao.CrashMonitorDao
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.crash.monitor.models.CrashMonitorParameters
import kotlinx.coroutines.flow.Flow

internal class CrashMonitorRepository(
    private val dao: CrashMonitorDao
) : Repositories.CrashMonitor {

    override suspend fun save(input: CrashMonitorParameters) {
        input.entity?.let { dao.save(it) }
            ?: throw IllegalStateException("Cannot save null entities")
    }

    override fun load(input: CrashMonitorParameters): Flow<CrashMonitorEntity> =
        dao.load()
}
