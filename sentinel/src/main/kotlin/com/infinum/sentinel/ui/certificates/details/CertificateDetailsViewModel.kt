package com.infinum.sentinel.ui.certificates.details

import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class CertificateDetailsViewModel(
    private val repository: Repositories.Certificate
) : BaseChildViewModel<CertificateDetailsState, Nothing>() {

    override fun data() =
        launch {
            val result = io {
                repository.consume()
            }
            setState(
                CertificateDetailsState.Cache(
                    value = result
                )
            )
        }
}
