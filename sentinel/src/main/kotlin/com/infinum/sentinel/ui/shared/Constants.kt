package com.infinum.sentinel.ui.shared

internal object Constants {
    const val BYTE_MULTIPLIER = 1000
    const val SHARE_MIME_TYPE = "text/plain"
    const val NOTIFICATIONS_CHANNEL_ID = "sentinel"

    object Keys {
        const val BUNDLE_ID = "KEY_BUNDLE_ID"
        const val SHOULD_REFRESH: String = "KEY_SHOULD_REFRESH"
        const val APPLICATION_NAME: String = "KEY_APPLICATION_NAME"
        const val CRASH_ID: String = "KEY_CRASH_ID"
        const val NOTIFY_INVALID_NOW: String = "KEY_NOTIFY_INVALID_NOW"
        const val NOTIFY_TO_EXPIRE: String = "KEY_NOTIFY_TO_EXPIRE"
        const val EXPIRE_IN_AMOUNT: String = "KEY_EXPIRE_IN_AMOUNT"
        const val EXPIRE_IN_UNIT: String = "KEY_EXPIRE_IN_UNIT"
    }
}
