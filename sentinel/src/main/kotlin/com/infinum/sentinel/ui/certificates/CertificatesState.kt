package com.infinum.sentinel.ui.certificates

import com.infinum.sentinel.data.models.raw.CertificateData

internal sealed class CertificatesState {

    data class Data(
        val value: List<CertificateData>
    ) : CertificatesState()
}
