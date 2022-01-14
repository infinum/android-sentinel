package com.infinum.sentinel.ui.certificates

import com.infinum.sentinel.data.models.raw.CertificateData
import com.infinum.sentinel.data.models.raw.certificates.CertificateType
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.certificate.models.CertificateParameters
import com.infinum.sentinel.domain.certificate.monitor.models.CertificateMonitorParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.first

internal class CertificatesViewModel(
    private val collectors: Factories.Collector,
    private val repository: Repositories.Certificate,
    private val monitor: Repositories.CertificateMonitor
) : BaseChildViewModel<CertificatesState, CertificatesEvent>() {

    override fun data() =
        launch {
            val result = io {
                val certificates = collectors.certificates()()
                val settings = monitor.load(CertificateMonitorParameters()).first()
                certificates to settings
            }
            setState(
                CertificatesState.Data(
                    userCertificates = result.first[CertificateType.USER].orEmpty(),
                    systemCertificates = result.first[CertificateType.SYSTEM].orEmpty(),
                    settings = result.second
                )
            )
        }

    fun cache(data: CertificateData) =
        launch {
            io {
                repository.cache(
                    CertificateParameters.Cache(
                        value = data
                    )
                )
            }
            emitEvent(CertificatesEvent.Cached())
        }
}
