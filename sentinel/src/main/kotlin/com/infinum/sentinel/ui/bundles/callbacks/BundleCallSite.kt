package com.infinum.sentinel.ui.bundles.callbacks

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.infinum.sentinel.R

internal enum class BundleCallSite(
    @DrawableRes val icon: Int,
    @StringRes val text: Int,
) {
    ACTIVITY_INTENT_EXTRAS(
        R.drawable.sentinel_ic_activity_intent_extras,
        R.string.sentinel_bundle_activity_intent_extras,
    ),
    ACTIVITY_SAVED_STATE(
        R.drawable.sentinel_ic_activity_saved_state,
        R.string.sentinel_bundle_activity_saved_state,
    ),
    FRAGMENT_ARGUMENTS(
        R.drawable.sentinel_ic_fragment_arguments,
        R.string.sentinel_fragment_arguments,
    ),
    FRAGMENT_SAVED_STATE(
        R.drawable.sentinel_ic_fragment_saved_state,
        R.string.sentinel_fragment_saved_state,
    ),
}
