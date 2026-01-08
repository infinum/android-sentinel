package com.infinum.sentinel.domain.certificate.models

import com.infinum.sentinel.data.models.raw.CertificateData
import com.infinum.sentinel.domain.shared.base.BaseParameters

internal sealed class CertificateParameters : BaseParameters {
    data class Cache(
        val value: CertificateData,
    ) : CertificateParameters()
}
