package com.infinum.sentinel.data.models.raw

import com.infinum.sentinel.data.models.raw.certificates.FingerprintData
import com.infinum.sentinel.data.models.raw.certificates.PublicKeyData
import com.infinum.sentinel.data.models.raw.certificates.SignatureData
import java.util.Date

internal data class CertificateData(
    val publicKey: PublicKeyData,
    val serialNumber: String,
    val version: Int,
    val signature: SignatureData,
    val issuerData: List<String>,
    val subjectData: List<String>,
    val startDate: Date,
    val endDate: Date,
    val fingerprint: FingerprintData
) {
    private val o = issuerData.find { name -> name.startsWith("O = ") }?.removePrefix("O = ")?.trim()
    private val cn = issuerData.find { name -> name.startsWith("CN = ") }?.removePrefix("CN = ")?.trim()
    private val ou = issuerData.find { name -> name.startsWith("OU = ") }?.removePrefix("OU = ")?.trim()

    val title
        get() = (o ?: cn)?.removeSuffix("\\")
    val subtitle
        get() = if (o.isNullOrBlank().not()) {
            cn ?: ou.orEmpty()
        } else {
            ""
        }
    val isValid: Boolean
        get() {
            val start = startDate.time
            val now = System.currentTimeMillis()
            val end = endDate.time
            return now in (start + 1) until end
        }
}
