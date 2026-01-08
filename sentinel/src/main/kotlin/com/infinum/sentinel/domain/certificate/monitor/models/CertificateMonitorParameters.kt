package com.infinum.sentinel.domain.certificate.monitor.models

import com.infinum.sentinel.data.models.local.CertificateMonitorEntity
import com.infinum.sentinel.domain.shared.base.BaseParameters

internal data class CertificateMonitorParameters(
    val entity: CertificateMonitorEntity? = null,
) : BaseParameters
