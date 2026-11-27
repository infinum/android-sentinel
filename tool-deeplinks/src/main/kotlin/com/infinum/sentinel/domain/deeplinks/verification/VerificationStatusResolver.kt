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
     * or [VerificationStatus.NOT_REGISTERED] when none is declared. When opening supported links is turned off, Android
     * approves no host, so a verified or allowed host gets its handling-off status.
     */
    fun resolve(
        scheme: String,
        host: String?,
        snapshot: DomainVerificationData?,
    ): VerificationStatus? =
        if (!isWeb(scheme) || host.isNullOrBlank() || snapshot == null) {
            null
        } else {
            statusOf(findState(host, snapshot.hostStates)).let { status ->
                if (snapshot.isLinkHandlingAllowed) status else whenHandlingOff(status)
            }
        }

    private fun isWeb(scheme: String): Boolean =
        scheme.equals("http", ignoreCase = true) || scheme.equals("https", ignoreCase = true)

    /**
     * A host is declared when a key or a wildcard key covers it the way intent filters match: ignoring case, and
     * `*.example.com` covers `shop.example.com` but not `example.com`. Its state is the best one among its own key and
     * the wildcard keys Android applies to it, because Android approves a host through any verified or selected key
     * (DomainVerificationService.approvalLevelForDomainInternal). That check compares the host as written, so it is
     * case-sensitive, and it also lets `*.example.com` cover `example.com`. A declared host that no key approves, such
     * as `EXAMPLE.com` for the key `example.com`, has no approval.
     */
    private fun findState(
        host: String,
        states: Map<String, Int>,
    ): Int? {
        val wildcards = states.keys.filter { it.startsWith(WILDCARD_PREFIX) }
        val isDeclared =
            states.keys.any { it.equals(host, ignoreCase = true) } ||
                wildcards.any { host.endsWith(filterSuffix(it), ignoreCase = true) }
        return if (isDeclared) {
            val applied = wildcards.filter { host.endsWith(approvalSuffix(it)) }.map { states.getValue(it) }
            (listOfNotNull(states[host]) + applied).maxByOrNull(::rankOf) ?: STATE_NONE
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

    private fun whenHandlingOff(status: VerificationStatus): VerificationStatus =
        when (status) {
            VerificationStatus.VERIFIED -> VerificationStatus.VERIFIED_HANDLING_OFF
            VerificationStatus.ENABLED_IN_SETTINGS -> VerificationStatus.ENABLED_HANDLING_OFF
            else -> status
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
