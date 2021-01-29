package com.infinum.sentinel.data.sources.raw.collectors

import android.content.Context
import android.content.pm.PackageManager
import com.infinum.sentinel.domain.collectors.Collectors

internal class PermissionsCollector(
    private val context: Context
) : Collectors.Permissions {

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
