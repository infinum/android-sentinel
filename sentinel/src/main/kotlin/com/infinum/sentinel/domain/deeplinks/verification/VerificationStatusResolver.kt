package com.infinum.sentinel.domain.deeplinks.verification

import com.infinum.sentinel.data.models.raw.deeplinks.DomainVerificationData
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus

/**
 * Maps a link host and a [DomainVerificationData] snapshot to a [VerificationStatus].
 */
internal object VerificationStatusResolver {
    // Documented DomainVerificationUserState domain states.
    private const val STATE_NONE = 0
    private const val STATE_SELECTED = 1
    private const val STATE_VERIFIED = 2

    private const val WILDCARD_PREFIX = "*."

    /**
     * Returns null when the scheme is not http or https, the host is null or blank, or there is no
     * snapshot. Otherwise returns the status of the exact host, or of the longest matching
     * wildcard host, or [VerificationStatus.NOT_REGISTERED] when none is declared.
     */
    fun resolve(
        scheme: String,
        host: String?,
        snapshot: DomainVerificationData?,
    ): VerificationStatus? =
        if (!isWeb(scheme) || host.isNullOrBlank() || snapshot == null) {
            null
        } else {
            statusOf(findState(host.lowercase(), snapshot.hostStates))
        }

    private fun isWeb(scheme: String): Boolean =
        scheme.equals("http", ignoreCase = true) || scheme.equals("https", ignoreCase = true)

    private fun findState(
        host: String,
        states: Map<String, Int>,
    ): Int? {
        val lowered = states.mapKeys { it.key.lowercase() }
        return lowered[host]
            ?: lowered.keys
                .filter { it.startsWith(WILDCARD_PREFIX) && host.endsWith(it.drop(1)) }
                .maxByOrNull { it.length }
                ?.let { lowered[it] }
    }

    private fun statusOf(state: Int?): VerificationStatus =
        when (state) {
            null -> VerificationStatus.NOT_REGISTERED
            STATE_VERIFIED -> VerificationStatus.VERIFIED
            STATE_SELECTED -> VerificationStatus.ENABLED_IN_SETTINGS
            STATE_NONE -> VerificationStatus.NOT_VERIFIED
            else -> VerificationStatus.NOT_VERIFIED
        }
}
