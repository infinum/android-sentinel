package com.infinum.sentinel.data.models.memory.triggers.airplanemode

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import com.infinum.sentinel.data.models.memory.triggers.AbstractTrigger
import com.infinum.sentinel.data.models.memory.triggers.shared.receiver.BroadcastReceiver

class AirplaneModeOnTrigger(
    private val context: Context,
    private val trigger: () -> Unit
) : AbstractTrigger() {

    companion object {
        private const val STATE = "state"
    }

    private val broadcastReceiverBuilder = BroadcastReceiver {
        onAction(Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            isEnabled(it.extras?.getBoolean(STATE, false) ?: false)
        }
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun start() {
        context.registerReceiver(
            broadcastReceiverBuilder.receiver,
            broadcastReceiverBuilder.filter
        )
        this.active = true
    }

    override fun stop() {
        context.unregisterReceiver(broadcastReceiverBuilder.receiver)
        this.active = false
    }

    private fun isEnabled(enabled: Boolean) {
        if (active && enabled) {
            trigger()
        }
    }
}
