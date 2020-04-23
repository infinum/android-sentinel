package com.infinum.sentinel.data.sources.raw

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.os.ConfigurationCompat
import com.infinum.sentinel.data.models.raw.ApplicationData
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

internal class ApplicationCollector(
    private val context: Context
) : AbstractCollector<ApplicationData>() {

    companion object {
        private const val FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss"

        @SuppressLint("ConstantLocale")
        private val dateFormatter = SimpleDateFormat(FORMAT_DATETIME, Locale.getDefault())
    }

    override lateinit var data: ApplicationData

    @Suppress("DEPRECATION")
    override fun collect() {
        with(context) {
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val currentLocale = ConfigurationCompat.getLocales(resources.configuration)[0]

            data = ApplicationData(
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toString()
                } else {
                    packageInfo.versionCode.toString()
                },
                versionName = packageInfo.versionName,
                firstInstall = dateFormatter.format(Date(packageInfo.firstInstallTime)),
                lastUpdate = dateFormatter.format(Date(packageInfo.lastUpdateTime)),
                minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    "${applicationInfo.minSdkVersion}"
                } else {
                    ""
                },
                targetSdk = "${applicationInfo.targetSdkVersion}",
                packageName = packageName,
                processName = applicationInfo.processName,
                taskAffinity = applicationInfo.taskAffinity,
                localeLanguage = currentLocale.language,
                localeCountry = currentLocale.country
            )
        }
    }
}
