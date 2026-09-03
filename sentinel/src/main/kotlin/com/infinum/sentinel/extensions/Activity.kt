package com.infinum.sentinel.extensions

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.infinum.sentinel.BuildConfig
import com.infinum.sentinel.R
import com.infinum.sentinel.ui.shared.base.BaseActivity

internal val Activity.isMonitoredScreen: Boolean
    get() =
        if (BuildConfig.DEBUG) {
            true
        } else {
            val metaKey = getString(R.string.sentinel_infinum_monitored)

            val metadata: Bundle? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager
                        .getActivityInfo(
                            this.componentName,
                            PackageManager.ComponentInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
                        ).metaData
                } else {
                    @Suppress("DEPRECATION")
                    packageManager
                        .getActivityInfo(this.componentName, PackageManager.GET_META_DATA)
                        .metaData
                }

            metadata?.let {
                if (it.containsKey(metaKey)) {
                    it.getBoolean(metaKey)
                } else {
                    true
                }
            } ?: true
        }

/**
 * Whether this is one of Sentinel's own screens rather than a screen of the app being debugged.
 *
 * [isMonitoredScreen] cannot answer this: it short-circuits to `true` whenever the library
 * itself was built as debug, which is exactly how Sentinel is consumed, so the
 * `sentinel_infinum_monitored=false` metadata on Sentinel's own activities never gets read.
 * Every Sentinel screen extends [BaseActivity], and it is `internal`, so no host app can, which
 * makes this both complete and impossible to get wrong by renaming a package.
 */
internal val Activity.isSentinelOwnScreen: Boolean
    get() = this is BaseActivity<*, *>
