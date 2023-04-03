package com.infinum.sentinel.data.sources.raw.collectors

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.extensions.isPermissionGranted
import me.tatarka.inject.annotations.Inject

@Inject
internal class PermissionsCollector(
    private val context: Context
) : Collectors.Permissions {

    override fun invoke(): Map<String, Boolean> {
        with(context) {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_PERMISSIONS
                )
            }

            return packageInfo.requestedPermissions
                ?.toList()
                .orEmpty()
                .associateWith { isPermissionGranted(it) }
        }
    }
}
