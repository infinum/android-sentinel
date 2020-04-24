package com.infinum.sentinel.data.sources.raw

import android.content.Context
import android.content.pm.PackageManager

internal class PermissionsCollector(
    private val context: Context
) : AbstractCollector<Map<String, Boolean>>() {

    override lateinit var data: Map<String, Boolean>

    override fun collect() {
        with(context) {
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS
            )
            data = with(packageInfo) {
                requestedPermissions?.toList().orEmpty()
                    .map {
                        it to (packageManager.checkPermission(
                            it,
                            packageName
                        ) == PackageManager.PERMISSION_GRANTED)
                    }.toMap()
            }
        }
    }
}
