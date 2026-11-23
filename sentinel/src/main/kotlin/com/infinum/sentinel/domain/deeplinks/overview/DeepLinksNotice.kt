package com.infinum.sentinel.domain.deeplinks.overview

/**
 * A notice shown above every section of the Deep links list.
 */
internal enum class DeepLinksNotice {
    /** The app's link handling is turned off in system settings, so no web link opens it (FR-41). */
    LINK_HANDLING_OFF,

    /** The manifest couldn't be read, so declared links are missing (FR-50). */
    DISCOVERY_FAILED,
}
