package com.infinum.sentinel.domain.deeplinks.models

/**
 * App Links verification status of a declared host.
 */
internal enum class VerificationStatus {
    /** The system verified the domain. */
    VERIFIED,

    /** The user allowed the domain in the app's link settings. */
    ENABLED_IN_SETTINGS,

    /** The system verified the domain, but opening supported links is turned off for the app. */
    VERIFIED_HANDLING_OFF,

    /** The user allowed the domain, but opening supported links is turned off for the app. */
    ENABLED_HANDLING_OFF,

    /** The domain is declared but neither verified nor allowed. */
    NOT_VERIFIED,

    /** The app does not declare this host for App Links. */
    NOT_REGISTERED,
}
