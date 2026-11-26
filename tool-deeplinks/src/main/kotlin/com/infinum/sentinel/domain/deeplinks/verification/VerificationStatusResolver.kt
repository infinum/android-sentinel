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
     * snapshot. Otherwise returns the best status among the host's own key and the wildcard keys that apply to it,
     * or [VerificationStatus.NOT_REGISTERED] when none is declared.
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

    /**
     * A host is declared when it is a key itself or a wildcard key covers it the way intent filters match:
     * `*.example.com` covers `shop.example.com` but not `example.com`. Its state is the best one among its own key and
     * the wildcard keys Android applies to it, because Android approves a host through any verified or selected key
     * (DomainVerificationService.approvalLevelForDomainInternal). That check also lets `*.example.com` cover
     * `example.com`.
     */
    private fun findState(
        host: String,
        states: Map<String, Int>,
    ): Int? {
        val lowered = states.mapKeys { it.key.lowercase() }
        val wildcards = lowered.keys.filter { it.startsWith(WILDCARD_PREFIX) }
        val isDeclared = host in lowered || wildcards.any { host.endsWith(filterSuffix(it)) }
        return if (isDeclared) {
            val applied = wildcards.filter { host.endsWith(approvalSuffix(it)) }.map { lowered.getValue(it) }
            (listOfNotNull(lowered[host]) + applied).maxByOrNull(::rankOf)
        } else {
            null
        }
    }

    /** `.example.com` for `*.example.com`: the suffix an intent filter's wildcard host matches. */
    private fun filterSuffix(wildcard: String): String = wildcard.drop(1)

    /** `example.com` for `*.example.com`: the suffix Android's approval check matches, with no dot boundary. */
    private fun approvalSuffix(wildcard: String): String = wildcard.drop(WILDCARD_PREFIX.length)

    private fun rankOf(state: Int): Int =
        when (state) {
            STATE_VERIFIED -> 2
            STATE_SELECTED -> 1
            else -> 0
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
