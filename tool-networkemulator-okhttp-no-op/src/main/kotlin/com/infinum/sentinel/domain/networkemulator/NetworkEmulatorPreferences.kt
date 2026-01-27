package com.infinum.sentinel.domain.networkemulator

import android.content.Context

/**
 * No-op implementation of Network Emulator Preferences.
 *
 * This implementation provides stub properties that always return default/disabled values.
 * All setter operations are no-ops. It is safe to include in release builds.
 *
 * This class maintains API compatibility with the debug implementation.
 */
public class NetworkEmulatorPreferences(
    @Suppress("UNUSED_PARAMETER") context: Context,
) {
    /**
     * Always returns false in no-op implementation.
     */
    public var isEnabled: Boolean
        get() = false
        set(@Suppress("UNUSED_PARAMETER") value) {
            // No-op
        }

    /**
     * Always returns 0 in no-op implementation.
     */
    public var delayMs: Int
        get() = 0
        set(@Suppress("UNUSED_PARAMETER") value) {
            // No-op
        }

    /**
     * Always returns 0 in no-op implementation.
     */
    public var failPercentage: Int
        get() = 0
        set(@Suppress("UNUSED_PARAMETER") value) {
            // No-op
        }

    /**
     * Always returns 0 in no-op implementation.
     */
    public var variancePercentage: Int
        get() = 0
        set(@Suppress("UNUSED_PARAMETER") value) {
            // No-op
        }

    /**
     * No-op reset operation.
     */
    public fun reset() {
        // No-op
    }
}
