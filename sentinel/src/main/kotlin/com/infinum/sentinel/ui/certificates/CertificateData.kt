package com.infinum.sentinel.ui.certificates

import java.util.Date

internal data class CertificateData(
    val publicKey: PublicKeyData,
    val serialNumber: String,
    val version: Int,
    val signature: SignatureData,
    val issuerName: String,
    val subjectName: String,
    val startDate: Date,
    val endDate: Date,
    val fingerprint: FingerprintData
)
