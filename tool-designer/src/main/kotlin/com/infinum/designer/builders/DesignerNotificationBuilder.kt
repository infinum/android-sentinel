package com.infinum.designer.builders

import android.app.Service
import androidx.core.app.NotificationCompat
import com.infinum.designer.R

class DesignerNotificationBuilder(
    private val service: Service
) {

    companion object {
        private const val CHANNEL_ID = "12345"
        private const val NOTIFICATION_ID = 555
    }

    private val intentBuilder =
        DesignerIntentBuilder(service)

    fun show() {
        NotificationCompat.Builder(service,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.designer_ic_pick)
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

}