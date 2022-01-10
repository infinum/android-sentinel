package com.infinum.sentinel.ui.certificates

import com.infinum.sentinel.data.models.raw.CertificateData

internal sealed class CertificatesState {

    data class Data(
        val userCertificates: List<CertificateData>,
        val systemCertificates: List<CertificateData>
    ) : CertificatesState()
}
