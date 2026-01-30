package com.infinum.sentinel.ui.certificates.observer

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.infinum.sentinel.data.models.raw.certificates.CertificateType
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.extensions.applicationName
import com.infinum.sentinel.ui.shared.Constants.Keys.EXPIRE_IN_AMOUNT
import com.infinum.sentinel.ui.shared.Constants.Keys.EXPIRE_IN_UNIT
import com.infinum.sentinel.ui.shared.Constants.Keys.NOTIFY_INVALID_NOW
import com.infinum.sentinel.ui.shared.Constants.Keys.NOTIFY_TO_EXPIRE
import com.infinum.sentinel.ui.shared.notification.NotificationFactory
import java.time.temporal.ChronoUnit

internal class CertificateCheckWorker(
    private val context: Context,
    parameters: WorkerParameters,
    private val collectors: Factories.Collector,
    private val notificationFactory: NotificationFactory,
) : CoroutineWorker(context, parameters) {
    companion object {
        const val NAME = "com.infinum.sentinel.ui.certificates.observer.CertificateCheckWorker"
    }

    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return Result.success()
        }
        val notifyInvalidNow = inputData.getBoolean(NOTIFY_INVALID_NOW, false)
        val notifyToExpire = inputData.getBoolean(NOTIFY_TO_EXPIRE, false)
        val expireInAmount = inputData.getInt(EXPIRE_IN_AMOUNT, 0)
        val expireInUnit =
            inputData.getString(EXPIRE_IN_UNIT)?.let {
                ChronoUnit.values().single { unit -> unit.name == it }
            } ?: ChronoUnit.DAYS

        if (notifyInvalidNow || notifyToExpire) {
            val userCertificates =
                collectors
                    .certificates()
                    .invoke()[CertificateType.USER]
                    .orEmpty()

            userCertificates
                .filterNot { certificate -> certificate.isValidNow }
                .count()
                .takeIf { it > 0 && notifyInvalidNow }
                ?.let { notificationFactory.showExpiredCertificate(context.applicationName, it) }

            userCertificates
                .filterNot { certificate -> certificate.isValidIn(expireInAmount, expireInUnit) }
                .count()
                .takeIf { it > 0 && notifyToExpire }
                ?.let { notificationFactory.showToExpireCertificate(context.applicationName, it) }
        }

        return Result.success()
    }
}
