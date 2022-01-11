package com.infinum.sentinel.ui.shared.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.CrashEntity

internal class SystemNotificationFactory(
    private val context: Context,
    private val intentFactory: IntentFactory
) : NotificationFactory {

    companion object {
        private const val NOTIFICATIONS_CHANNEL_ID = "sentinel_crashes"
        private const val NOTIFICATION_ID_CRASH = 3105
        private const val NOTIFICATION_ID_CERTIFICATE = 7011
        private const val NOTIFICATION_ID_CERTIFICATE_EXPIRE = 1802
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATIONS_CHANNEL_ID,
                context.getString(R.string.sentinel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannels(listOf(notificationChannel))
        }
    }

    override fun showCrash(applicationName: String, id: Long, entity: CrashEntity) {
        buildExceptionNotification(applicationName, id, entity)
    }

    override fun showAnr(applicationName: String, id: Long, entity: CrashEntity) {
        buildAnrNotification(applicationName, id, entity)
    }

    override fun showExpiredCertificate(applicationName: String, count: Int) {
        buildExpiredCertificateNotification(applicationName, count)
    }

    override fun showToExpireCertificate(applicationName: String, count: Int) {
        buildToExpireCertificateNotification(applicationName, count)
    }

    private fun buildExceptionNotification(applicationName: String, id: Long, entity: CrashEntity) =
        buildNotification(
            NOTIFICATION_ID_CRASH,
            entity.timestamp,
            R.drawable.sentinel_ic_notification_crash,
            String.format(
                context.getString(R.string.sentinel_crash_notification_title),
                entity.applicationName
            ),
            context.getString(R.string.sentinel_anr_notification_subtitle),
            intentFactory.crash(applicationName, id)
        )

    private fun buildAnrNotification(applicationName: String, id: Long, entity: CrashEntity) =
        buildNotification(
            NOTIFICATION_ID_CRASH,
            entity.timestamp,
            R.drawable.sentinel_ic_notification_anr,
            String.format(
                context.getString(R.string.sentinel_anr_notification_title),
                entity.applicationName
            ),
            context.getString(R.string.sentinel_anr_notification_subtitle),
            intentFactory.crash(applicationName, id)
        )

    private fun buildExpiredCertificateNotification(applicationName: String, count: Int) =
        buildNotification(
            NOTIFICATION_ID_CERTIFICATE,
            System.currentTimeMillis(),
            R.drawable.sentinel_ic_notification_certificate_invalid,
            String.format(
                context.getString(R.string.sentinel_certificates_notification_title),
                count
            ),
            context.getString(R.string.sentinel_certificates_notification_subtitle),
            intentFactory.certificate(applicationName)
        )

    private fun buildToExpireCertificateNotification(applicationName: String, count: Int) =
        buildNotification(
            NOTIFICATION_ID_CERTIFICATE_EXPIRE,
            System.currentTimeMillis(),
            R.drawable.sentinel_ic_notification_certificate_invalid,
            String.format(
                context.getString(R.string.sentinel_certificates_soon_notification_title),
                count
            ),
            context.getString(R.string.sentinel_certificates_notification_subtitle),
            intentFactory.certificate(applicationName)
        )

    @Suppress("LongParameterList")
    private fun buildNotification(
        notificationId: Int,
        timestamp: Long,
        @DrawableRes smallIcon: Int,
        title: String,
        content: String,
        intents: Array<Intent>,
    ) {
        val builder = NotificationCompat.Builder(context, NOTIFICATIONS_CHANNEL_ID)
            .setContentIntent(buildPendingIntent(intents))
            .setLocalOnly(true)
            .setSmallIcon(smallIcon)
            .setColor(ContextCompat.getColor(context, R.color.sentinel_color_primary))
            .setContentTitle(title)
            .setContentText(content)
            .setWhen(timestamp)
            .setShowWhen(true)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun buildPendingIntent(intents: Array<Intent>) =
        PendingIntent.getActivities(
            context,
            NOTIFICATION_ID_CRASH,
            intents,
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                    PendingIntent.FLAG_MUTABLE
                else -> PendingIntent.FLAG_CANCEL_CURRENT
            }
        )
}
