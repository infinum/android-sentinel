package com.infinum.sentinel.domain.deeplinks.models

/**
 * App Links verification status of a declared host.
 */
internal enum class VerificationStatus {
    /** The system verified the domain. */
    VERIFIED,

    /** The user allowed the domain in the app's link settings. */
    ENABLED_IN_SETTINGS,

    /** The domain is declared but neither verified nor allowed. */
    NOT_VERIFIED,

    /** The app does not declare this host for App Links. */
    NOT_REGISTERED,
}
