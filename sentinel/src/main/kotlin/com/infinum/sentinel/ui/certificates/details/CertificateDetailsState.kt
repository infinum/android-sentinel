package com.infinum.sentinel.ui.certificates.details

import com.infinum.sentinel.data.models.local.CertificateMonitorEntity
import com.infinum.sentinel.data.models.raw.CertificateData

internal sealed class CertificateDetailsState {
    data class Cache(
        val value: CertificateData,
        val settings: CertificateMonitorEntity,
    ) : CertificateDetailsState()
}
