package com.infinum.sentinel.domain.networkemulator

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages network emulator configuration preferences.
 * Stores settings for network delay, failure rate, and variance.
 */
public class NetworkEmulatorPreferences(
    context: Context,
) {
    public companion object {
        private const val PREF_NAME = "sentinel_network_emulator"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_DELAY_MS = "delay_ms"
        private const val KEY_FAIL_PERCENTAGE = "fail_percentage"
        private const val KEY_VARIANCE_PERCENTAGE = "variance_percentage"

        private const val DEFAULT_ENABLED = false
        private const val DEFAULT_DELAY_MS = 1000
        private const val DEFAULT_FAIL_PERCENTAGE = 0
        private const val DEFAULT_VARIANCE_PERCENTAGE = 0
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Whether network emulation is currently enabled
     */
    public var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, DEFAULT_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    /**
     * Network delay in milliseconds (default: 1000ms)
     */
    public var delayMs: Int
        get() = prefs.getInt(KEY_DELAY_MS, DEFAULT_DELAY_MS)
        set(value) = prefs.edit().putInt(KEY_DELAY_MS, value.coerceIn(0, 15000)).apply()

    /**
     * Percentage of requests that should fail (0-100)
     */
    public var failPercentage: Int
        get() = prefs.getInt(KEY_FAIL_PERCENTAGE, DEFAULT_FAIL_PERCENTAGE)
        set(value) = prefs.edit().putInt(KEY_FAIL_PERCENTAGE, value.coerceIn(0, 100)).apply()

    /**
     * Variance in network delay as percentage (0-100)
     */
    public var variancePercentage: Int
        get() = prefs.getInt(KEY_VARIANCE_PERCENTAGE, DEFAULT_VARIANCE_PERCENTAGE)
        set(value) = prefs.edit().putInt(KEY_VARIANCE_PERCENTAGE, value.coerceIn(0, 100)).apply()

    /**
     * Reset all settings to defaults
     */
    public fun reset() {
        prefs
            .edit()
            .apply {
                putBoolean(KEY_ENABLED, DEFAULT_ENABLED)
                putInt(KEY_DELAY_MS, DEFAULT_DELAY_MS)
                putInt(KEY_FAIL_PERCENTAGE, DEFAULT_FAIL_PERCENTAGE)
                putInt(KEY_VARIANCE_PERCENTAGE, DEFAULT_VARIANCE_PERCENTAGE)
            }.apply()
    }
}
