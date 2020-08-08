package com.infinum.sentinel.data.sources.raw

import android.content.Context
import android.content.pm.PackageManager

internal class PermissionsCollector(
    private val context: Context
) : Collector<Map<String, Boolean>> {

    override fun invoke(): Map<String, Boolean> {
        with(context) {
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS
            )

            return with(packageInfo) {
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
