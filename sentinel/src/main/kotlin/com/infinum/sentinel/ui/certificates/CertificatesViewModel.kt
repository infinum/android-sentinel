package com.infinum.sentinel.ui.certificates

import com.infinum.sentinel.data.models.raw.CertificateData
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.certificate.models.CertificateParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class CertificatesViewModel(
    private val collectors: Factories.Collector,
    private val repository: Repositories.Certificate
) : BaseChildViewModel<CertificatesState, CertificatesEvent>() {

    override fun data() =
        launch {
            val result = io {
                collectors.certificates()()
                    .sortedBy { it.title?.lowercase() }
            }
            setState(
                CertificatesState.Data(
                    value = result
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
