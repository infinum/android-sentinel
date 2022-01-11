package com.infinum.sentinel.ui.certificates.observer

import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.infinum.sentinel.BuildConfig
import java.time.Duration

internal class SentinelWorkManager(
    private val context: Context
) {

    companion object {
        private const val DEBUG_INTERVAL = 15L
        private const val RELEASE_INTERVAL = 1440L
    }

    init {
        WorkManager.initialize(
            context,
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
        )
    }

    fun checkCertificates() {
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "sentinel_check_certificates",
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<SentinelWorker>(
                    when (BuildConfig.DEBUG) {
                        true -> Duration.ofMinutes(DEBUG_INTERVAL)
                        false -> Duration.ofMinutes(RELEASE_INTERVAL)
                    }
                )
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                            .setRequiresCharging(false)
                            .setRequiresBatteryNotLow(true)
                            .apply {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    setRequiresDeviceIdle(false)
                                }
                            }
                            .build()
                    )
                    .build()
            )
    }
}
