package com.infinum.sentinel.ui.certificates.observer

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.ui.shared.notification.NotificationFactory
import me.tatarka.inject.annotations.Inject

@Inject
internal class SentinelWorkerFactory(
    private val collectors: Factories.Collector,
    private val notificationFactory: NotificationFactory
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? =
        when (workerClassName) {
            CertificateCheckWorker.NAME ->
                CertificateCheckWorker(
                    appContext,
                    workerParameters,
                    collectors,
                    notificationFactory
                )
            else -> null
        }
}
