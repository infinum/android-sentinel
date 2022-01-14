package com.infinum.sentinel.ui.certificates.observer

import android.content.Context
import android.os.Build
import androidx.lifecycle.asFlow
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.infinum.sentinel.BuildConfig
import com.infinum.sentinel.data.models.local.CertificateMonitorEntity
import com.infinum.sentinel.ui.Presentation.Constants.Keys.EXPIRE_IN_AMOUNT
import com.infinum.sentinel.ui.Presentation.Constants.Keys.EXPIRE_IN_UNIT
import com.infinum.sentinel.ui.Presentation.Constants.Keys.NOTIFY_INVALID_NOW
import com.infinum.sentinel.ui.Presentation.Constants.Keys.NOTIFY_TO_EXPIRE
import java.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

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

    fun startCertificatesCheck(entity: CertificateMonitorEntity) =
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                CertificateCheckWorker.NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<CertificateCheckWorker>(
                    when (BuildConfig.DEBUG) {
                        true -> Duration.ofMinutes(DEBUG_INTERVAL)
                        false -> Duration.ofMinutes(RELEASE_INTERVAL)
                    }
                )
                    .setInputData(
                        workDataOf(
                            NOTIFY_INVALID_NOW to entity.notifyInvalidNow,
                            NOTIFY_TO_EXPIRE to entity.notifyToExpire,
                            EXPIRE_IN_AMOUNT to entity.expireInAmount,
                            EXPIRE_IN_UNIT to entity.expireInUnit.name
                        )
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

    fun certificatesCheckState(): Flow<Boolean> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(CertificateCheckWorker.NAME)
            .get()
            .firstOrNull()
            ?.id
            ?.let {
                WorkManager.getInstance(context)
                    .getWorkInfoByIdLiveData(it)
                    .asFlow()
                    .map { info -> info?.state != WorkInfo.State.CANCELLED }
            } ?: flowOf(false)

    fun stopCertificatesCheck() =
        WorkManager.getInstance(context)
            .cancelUniqueWork(CertificateCheckWorker.NAME)
}
