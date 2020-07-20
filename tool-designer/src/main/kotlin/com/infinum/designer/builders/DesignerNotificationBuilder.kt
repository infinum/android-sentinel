package com.infinum.designer.builders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.infinum.designer.R

class DesignerNotificationBuilder(
    private val service: Service
) {

    companion object {
        private const val NOTIFICATION_ID = 555
    }

    private val intentBuilder =
        DesignerIntentBuilder(service)

    fun show() {
        NotificationCompat.Builder(
            service,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("Designer tools", "Designer tools foreground service")
            } else {
                "Designer tools"
            }
        )
            .setSmallIcon(R.drawable.designer_ic_logo)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentTitle(service.getString(R.string.designer_title))
            .setContentIntent(intentBuilder.settings())
            .setDeleteIntent(intentBuilder.stop())
            .addAction(
                NotificationCompat.Action(
                    0,
                    "Settings",
                    intentBuilder.settings()
                )
            )
            .addAction(
                NotificationCompat.Action(
                    0,
                    "Reset",
                    intentBuilder.reset()
                )
            )
            .addAction(
                NotificationCompat.Action(
                    0,
                    "Stop",
                    intentBuilder.stop()
                )
            )
            .build()
            .also {
                service.startForeground(NOTIFICATION_ID, it)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        (service.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.let {
            it.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    lightColor = Color.BLUE
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
            )
        }
        return channelId
    }

}