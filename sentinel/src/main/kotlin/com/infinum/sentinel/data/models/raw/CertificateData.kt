package com.infinum.sentinel.data.models.raw

import com.infinum.sentinel.data.models.raw.certificates.FingerprintData
import com.infinum.sentinel.data.models.raw.certificates.PublicKeyData
import com.infinum.sentinel.data.models.raw.certificates.SignatureData
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit
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
    val isValidNow: Boolean
        get() {
            val start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val now = LocalDate.now()
            val end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

            return now.isAfter(start) && now.isBefore(end)
        }

    fun isValidIn(amount: Int = 0, unit: ChronoUnit = ChronoUnit.DAYS): Boolean {
        val delta: Period = when (unit) {
            ChronoUnit.DAYS -> Period.ofDays(amount)
            ChronoUnit.WEEKS -> Period.ofWeeks(amount)
            ChronoUnit.MONTHS -> Period.ofMonths(amount)
            ChronoUnit.YEARS -> Period.ofYears(amount)
            else -> Period.ZERO
        }

        val start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val now = LocalDate.now().plus(delta)
        val end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        return now.isAfter(start) && now.isBefore(end)
    }
}
