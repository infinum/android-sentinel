package com.infinum.sentinel.domain.certificate.monitor

import com.infinum.sentinel.data.models.local.CertificateMonitorEntity
import com.infinum.sentinel.data.sources.local.room.dao.CertificateMonitorDao
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.certificate.monitor.models.CertificateMonitorParameters
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
internal class CertificateMonitorRepository(
    private val dao: CertificateMonitorDao
) : Repositories.CertificateMonitor {

    override suspend fun save(input: CertificateMonitorParameters) {
        input.entity?.let { dao.save(it) }
            ?: error("Cannot save null entities")
    }

    override fun load(input: CertificateMonitorParameters): Flow<CertificateMonitorEntity> =
        dao.load()
}
