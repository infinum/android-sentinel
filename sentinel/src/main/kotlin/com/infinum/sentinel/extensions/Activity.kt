package com.infinum.sentinel.extensions

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.infinum.sentinel.BuildConfig
import com.infinum.sentinel.R

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
