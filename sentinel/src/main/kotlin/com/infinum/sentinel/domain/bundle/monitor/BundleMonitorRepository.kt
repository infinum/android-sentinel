package com.infinum.sentinel.domain.bundle.monitor

import com.infinum.sentinel.data.models.local.BundleMonitorEntity
import com.infinum.sentinel.data.sources.local.room.dao.BundleMonitorDao
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import kotlinx.coroutines.flow.Flow

internal class BundleMonitorRepository(
    private val dao: BundleMonitorDao
) : Repositories.BundleMonitor {

    override suspend fun save(input: BundleMonitorParameters) {
        input.entity?.let { dao.save(it) }
            ?: throw IllegalStateException("Cannot save null entities")
    }

    override fun load(input: BundleMonitorParameters): Flow<BundleMonitorEntity> =
        dao.load()
}
