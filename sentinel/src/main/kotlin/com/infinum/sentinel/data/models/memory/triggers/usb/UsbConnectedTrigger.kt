package com.infinum.sentinel.data.models.memory.triggers.usb

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.infinum.sentinel.data.models.memory.triggers.AbstractTrigger
import com.infinum.sentinel.data.models.memory.triggers.shared.receiver.BroadcastReceiver

class UsbConnectedTrigger(
    private val context: Context,
    private val trigger: () -> Unit
) : AbstractTrigger() {

    companion object {
        private const val USB_STATE = "android.hardware.usb.action.USB_STATE"
        private const val USB_CONNECTED = "connected"
    }

    private val broadcastReceiverBuilder = BroadcastReceiver {
        onAction(USB_STATE) {
            isConnected(it.extras?.getBoolean(USB_CONNECTED, false) ?: false)
        }
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun start() {
        context.registerReceiver(
            broadcastReceiverBuilder.broadcastReceiver,
            broadcastReceiverBuilder.filter
        )
        this.active = true
    }

    override fun stop() {
        context.unregisterReceiver(broadcastReceiverBuilder.broadcastReceiver)
        this.active = false
    }

    private fun isConnected(connected: Boolean) {
        if (active && connected) {
            trigger()
        }
    }
}