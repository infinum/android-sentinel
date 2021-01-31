package com.infinum.sentinel.data.models.memory.triggers.shared

import android.content.Context
import com.infinum.sentinel.data.models.memory.triggers.shared.receiver.BroadcastReceiver

internal abstract class BroadcastReceiverTrigger(
    private val context: Context
) : AbstractTrigger() {

    abstract val broadcastReceiver: BroadcastReceiver

    override fun start() {
        context.registerReceiver(
            broadcastReceiver.receiver,
            broadcastReceiver.filter
        )
        this.active = true
    }

    override fun stop() {
        context.unregisterReceiver(broadcastReceiver.receiver)
        this.active = false
    }
}
