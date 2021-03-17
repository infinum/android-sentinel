package com.infinum.sentinel.extensions

import android.app.Activity
import com.infinum.sentinel.BuildConfig
import com.infinum.sentinel.ui.bundles.BundlesActivity
import com.infinum.sentinel.ui.bundles.details.BundleDetailsActivity
import com.infinum.sentinel.ui.main.SentinelActivity
import com.infinum.sentinel.ui.settings.SettingsActivity

internal val Activity.isNotInternalScreen: Boolean
    get() = if (BuildConfig.DEBUG) {
        true
    } else {
        (this is SentinelActivity || this is SettingsActivity ||
            this is BundlesActivity || this is BundleDetailsActivity).not()
    }
