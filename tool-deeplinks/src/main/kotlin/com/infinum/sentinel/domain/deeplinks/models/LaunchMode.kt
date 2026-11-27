package com.infinum.sentinel.domain.deeplinks.models

/**
 * How a deep link is launched. Web links ask for the mode each time; other links use [REAL_TAP].
 */
internal enum class LaunchMode {
    REAL_TAP,
    OPEN_IN_APP,
}
