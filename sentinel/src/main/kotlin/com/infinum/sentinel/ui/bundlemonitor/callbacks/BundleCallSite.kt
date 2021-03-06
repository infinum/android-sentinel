package com.infinum.sentinel.ui.bundlemonitor.callbacks

internal enum class BundleCallSite {
    ACTIVITY_INTENT_EXTRAS,
    ACTIVITY_SAVED_STATE,
    FRAGMENT_ARGUMENTS,
    FRAGMENT_SAVED_STATE
}