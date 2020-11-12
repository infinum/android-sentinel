package com.infinum.sentinel.data.models.memory.triggers.usb

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.infinum.sentinel.data.models.memory.triggers.AbstractTrigger
import com.infinum.sentinel.data.models.memory.triggers.shared.receiver.BroadcastReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class UsbConnectedTrigger(
    private val context: Context,
    private val trigger: () -> Unit
) : AbstractTrigger() {

    companion object {
        private const val USB_STATE = "android.hardware.usb.action.USB_STATE"
        private const val USB_CONNECTED = "connected"
    }

    private var skippedFirst: Boolean = false

    private val broadcastReceiverBuilder = BroadcastReceiver {
        onAction(USB_STATE) {
            isConnected(it.extras?.getBoolean(USB_CONNECTED, false) ?: false)
        }
    }

    init {
        GlobalScope.launch(Dispatchers.Main) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this@UsbConnectedTrigger)
        }
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

    private fun isConnected(connected: Boolean) {
        if (skippedFirst) {
            if (active && connected) {
                trigger()
            }
        } else {
            skippedFirst = true
        }
    }
}
