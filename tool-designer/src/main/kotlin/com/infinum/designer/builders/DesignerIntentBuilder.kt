package com.infinum.designer.builders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.infinum.designer.ui.DesignerActivity
import com.infinum.designer.ui.DesignerService
import com.infinum.designer.ui.models.ServiceAction

class DesignerIntentBuilder(
    private val context: Context
) {

    fun settings(): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, DesignerActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )


    fun reset(): PendingIntent =
        PendingIntent.getService(
            context,
            0,
            Intent(context, DesignerService::class.java).apply {
                action = ServiceAction.RESET.code
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )


    fun stop(): PendingIntent =
        PendingIntent.getService(
            context,
            0,
            Intent(context, DesignerService::class.java).apply {
                action = ServiceAction.STOP.code
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )

}