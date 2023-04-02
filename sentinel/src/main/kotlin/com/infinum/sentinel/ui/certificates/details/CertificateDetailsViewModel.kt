package com.infinum.sentinel.ui.certificates.details

import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.certificate.monitor.models.CertificateMonitorParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject

@Inject
internal class CertificateDetailsViewModel(
    private val repository: Repositories.Certificate,
    private val monitor: Repositories.CertificateMonitor
) : BaseChildViewModel<CertificateDetailsState, Nothing>() {

    override fun data() =
        launch {
            val result = io {
                val item = repository.consume()
                val settings = monitor.load(CertificateMonitorParameters()).first()
                item to settings
            }
            setState(
                CertificateDetailsState.Cache(
                    value = result.first,
                    settings = result.second
                )
            )
        }
}
