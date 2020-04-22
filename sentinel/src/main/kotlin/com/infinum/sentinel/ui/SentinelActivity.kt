package com.infinum.sentinel.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.FragmentActivity
import com.infinum.sentinel.data.models.raw.AppInfo
import com.infinum.sentinel.data.sources.raw.DataSource
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@RestrictTo(RestrictTo.Scope.LIBRARY)
class SentinelActivity : FragmentActivity() {

    companion object {
        private const val FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss"
    }

    override fun onCreate(savedInstanceState: Bundle?) =
        super.onCreate(savedInstanceState)
            .run {
                DataSource.applicationIcon = applicationInfo.loadIcon(packageManager)
                DataSource.applicationData = collectApplicationData()
                DataSource.permissions = collectPermissions()

                SentinelFragment().show(supportFragmentManager, SentinelFragment.TAG)
            }

    override fun onPause() =
        super.onPause().run {
            overridePendingTransition(0, 0)
        }

    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun collectApplicationData(): Map<AppInfo, String> {
        val data = mutableMapOf<AppInfo, String>()
        data[AppInfo.NAME] = applicationInfo.loadLabel(packageManager).toString()
        with(packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)) {
            data[AppInfo.VERSION_CODE] =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    longVersionCode.toString()
                } else {
                    versionCode.toString()
                }
            data[AppInfo.VERSION_NAME] = versionName
            data[AppInfo.FIRST_INSTALL] =
                SimpleDateFormat(
                    FORMAT_DATETIME,
                    Locale.getDefault()
                ).format(Date(firstInstallTime))
            data[AppInfo.LAST_UPDATE] =
                SimpleDateFormat(FORMAT_DATETIME, Locale.getDefault()).format(Date(lastUpdateTime))
        }
        with(applicationInfo) {
            data[AppInfo.MIN_SDK] =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) "$minSdkVersion" else ""
            data[AppInfo.TARGET_SDK] = "$targetSdkVersion"
            data[AppInfo.PACKAGE_NAME] = packageName
            data[AppInfo.PROCESS_NAME] = processName
            data[AppInfo.TASK_AFFINITY] = taskAffinity

            val currentLocale = ConfigurationCompat.getLocales(resources.configuration)[0]
            data[AppInfo.LOCALE_LANGUAGE] = currentLocale.language
            data[AppInfo.LOCALE_COUNTRY] = currentLocale.country
        }

        return data.filter { it.value.isBlank().not() }
    }

    private fun collectPermissions(): Map<String, Boolean> =
        with(packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)) {
            requestedPermissions?.toList().orEmpty()
                .map {
                    it to (packageManager.checkPermission(
                        it,
                        packageName
                    ) == PackageManager.PERMISSION_GRANTED)
                }.toMap()
        }
}
