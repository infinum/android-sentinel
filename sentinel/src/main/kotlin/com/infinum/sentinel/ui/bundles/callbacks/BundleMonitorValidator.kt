package com.infinum.sentinel.ui.bundles.callbacks

import android.app.Activity
import com.infinum.sentinel.BuildConfig

internal interface BundleMonitorValidator {

    fun includeInternal(activity: Activity): Boolean =
        if (BuildConfig.DEBUG) {
            true
        } else {
            activity.packageName.startsWith(BuildConfig.LIBRARY_PACKAGE_NAME).not()
        }
}
