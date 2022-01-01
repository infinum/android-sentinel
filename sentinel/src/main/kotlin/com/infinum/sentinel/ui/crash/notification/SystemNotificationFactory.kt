package com.infinum.sentinel.ui.crash.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.CrashEntity
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.crash.CrashesActivity
import com.infinum.sentinel.ui.crash.details.CrashDetailsActivity

internal class SystemNotificationFactory(
    private val context: Context
) : NotificationFactory {

    companion object {
        private const val NOTIFICATIONS_CHANNEL_ID = "collar_analytics"
        private const val NOTIFICATION_ID = 3105
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
        buildNotification(applicationName, id, entity)
    }

    private fun buildNotification(applicationName: String, id: Long, entity: CrashEntity) {
        val builder = NotificationCompat.Builder(context, NOTIFICATIONS_CHANNEL_ID)
            .setContentIntent(
                PendingIntent.getActivities(
                    context,
                    NOTIFICATION_ID,
                    arrayOf(
                        Intent(context, CrashesActivity::class.java)
                            .apply {
                                putExtra(Presentation.Constants.Keys.APPLICATION_NAME, applicationName)
                            },
                        Intent(context, CrashDetailsActivity::class.java)
                            .apply {
                                putExtra(Presentation.Constants.Keys.CRASH_ID, id)
                            }
                    ),
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                            PendingIntent.FLAG_MUTABLE
                        else -> PendingIntent.FLAG_CANCEL_CURRENT
                    }
                )
            )
            .setLocalOnly(true)
            .setSmallIcon(R.drawable.sentinel_ic_notification_crash)
            .setColor(ContextCompat.getColor(context, R.color.sentinel_color_primary))
            .setContentTitle(
                String.format(
                    context.getString(R.string.sentinel_crash_notification_title),
                    entity.applicationName
                )
            )
            .setContentText(context.getString(R.string.sentinel_crash_notification_subtitle))
            .setWhen(entity.timestamp)
            .setShowWhen(true)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
