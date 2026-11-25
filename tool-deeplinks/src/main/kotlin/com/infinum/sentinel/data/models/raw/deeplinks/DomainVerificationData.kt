package com.infinum.sentinel.data.models.raw.deeplinks

/**
 * Raw snapshot of the app's `DomainVerificationUserState`.
 *
 * @property isLinkHandlingAllowed whether the user allows the app to open supported links.
 * @property hostStates declared host to its domain state. Values are the documented
 * `DomainVerificationUserState` domain states.
 */
internal data class DomainVerificationData(
    val isLinkHandlingAllowed: Boolean,
    val hostStates: Map<String, Int>,
)
