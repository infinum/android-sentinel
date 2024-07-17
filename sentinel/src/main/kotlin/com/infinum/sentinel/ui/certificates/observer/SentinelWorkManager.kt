package com.infinum.sentinel.ui.certificates.observer

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.workDataOf
import com.infinum.sentinel.BuildConfig
import com.infinum.sentinel.data.models.local.CertificateMonitorEntity
import com.infinum.sentinel.ui.shared.Constants.Keys.EXPIRE_IN_AMOUNT
import com.infinum.sentinel.ui.shared.Constants.Keys.EXPIRE_IN_UNIT
import com.infinum.sentinel.ui.shared.Constants.Keys.NOTIFY_INVALID_NOW
import com.infinum.sentinel.ui.shared.Constants.Keys.NOTIFY_TO_EXPIRE
import com.infinum.sentinel.ui.shared.Constants.Keys.WORKER_CLASS_NAME
import com.infinum.sentinel.ui.shared.Constants.Keys.WORKER_ID
import java.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@Inject
internal class SentinelWorkManager(
    private val context: Context,
    private val workerFactory: WorkerFactory
) {

    companion object {
        private const val DEBUG_INTERVAL = 15L
        private const val RELEASE_INTERVAL = 1440L
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startCertificatesCheck(entity: CertificateMonitorEntity) {

        val delegatedWorkData = workDataOf(
            WORKER_CLASS_NAME to CertificateCheckWorker::class.qualifiedName,
            WORKER_ID to CertificateCheckWorker.NAME,
            NOTIFY_INVALID_NOW to entity.notifyInvalidNow,
            NOTIFY_TO_EXPIRE to entity.notifyToExpire,
            EXPIRE_IN_AMOUNT to entity.expireInAmount,
            EXPIRE_IN_UNIT to entity.expireInUnit.name
        )
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DelegateWorker.DELEGATE_WORKER_ID,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                PeriodicWorkRequestBuilder<DelegateWorker>(
                    when (BuildConfig.DEBUG) {
                        true -> Duration.ofMinutes(DEBUG_INTERVAL)
                        false -> Duration.ofMinutes(RELEASE_INTERVAL)
                    }
                ).setInputData(delegatedWorkData)
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
