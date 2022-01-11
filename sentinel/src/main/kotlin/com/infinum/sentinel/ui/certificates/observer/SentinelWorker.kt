package com.infinum.sentinel.ui.certificates.observer

import android.content.Context
import androidx.work.WorkerParameters
import com.infinum.sentinel.data.models.raw.certificates.CertificateType
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.extensions.applicationName
import com.infinum.sentinel.ui.shared.base.BaseWorker
import com.infinum.sentinel.ui.shared.notification.NotificationFactory
import org.koin.core.component.inject

internal class SentinelWorker(
    private val context: Context,
    parameters: WorkerParameters
) : BaseWorker(context, parameters) {

    private val collectors: Factories.Collector by inject()
    private val notificationFactory: NotificationFactory by inject()

    override suspend fun doWork(): Result {
        val userCertificates = collectors.certificates()
            .invoke()[CertificateType.USER]
            .orEmpty()

        userCertificates
            .filterNot { certificate -> certificate.isValidNow }
            .count()
            .takeIf { it > 0 }
            ?.let { notificationFactory.showExpiredCertificate(context.applicationName, it) }

        userCertificates
            .filterNot { certificate -> certificate.isValidIn() }
            .count()
            .takeIf { it > 0 }
            ?.let { notificationFactory.showToExpireCertificate(context.applicationName, it) }

        return Result.success()
    }
}
