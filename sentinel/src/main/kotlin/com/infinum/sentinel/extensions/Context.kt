package com.infinum.sentinel.extensions

import android.content.Context
import android.content.pm.PackageManager

internal fun Context.isPermissionGranted(name: String): Boolean =
    packageManager.checkPermission(name, packageName) == PackageManager.PERMISSION_GRANTED
