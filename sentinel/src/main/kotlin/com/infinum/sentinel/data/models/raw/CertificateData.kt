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

    @Suppress("MagicNumber", "ComplexMethod")
    fun isValidIn(amount: Int = 2, unit: ChronoUnit = ChronoUnit.YEARS): Boolean {
        val delta: Period = when (unit) {
            ChronoUnit.NANOS -> Period.ZERO
            ChronoUnit.MICROS -> Period.ZERO
            ChronoUnit.MILLIS -> Period.ZERO
            ChronoUnit.SECONDS -> Period.ZERO
            ChronoUnit.MINUTES -> Period.ZERO
            ChronoUnit.HOURS -> Period.ZERO
            ChronoUnit.HALF_DAYS -> Period.ZERO
            ChronoUnit.DAYS -> Period.ofDays(amount)
            ChronoUnit.WEEKS -> Period.ofWeeks(amount)
            ChronoUnit.MONTHS -> Period.ofMonths(amount)
            ChronoUnit.YEARS -> Period.ofYears(amount)
            ChronoUnit.DECADES -> Period.ofYears(amount * 10)
            ChronoUnit.CENTURIES -> Period.ofYears(amount * 100)
            ChronoUnit.MILLENNIA -> Period.ofYears(amount * 1000)
            ChronoUnit.ERAS -> Period.ofYears(amount * 1000)
            ChronoUnit.FOREVER -> Period.ofYears(amount * 1000)
            else -> Period.ZERO
        }

        val start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val now = LocalDate.now().plus(delta)
        val end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        return now.isAfter(start) && now.isBefore(end)
    }
}
