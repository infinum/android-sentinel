package com.infinum.sentinel.data.models.memory.triggers.usb

import android.content.Context
import com.infinum.sentinel.data.models.memory.triggers.shared.BroadcastReceiverTrigger
import com.infinum.sentinel.data.models.memory.triggers.shared.receiver.BroadcastReceiver
import me.tatarka.inject.annotations.Inject

@Inject
internal class UsbConnectedTrigger(
    context: Context,
    private val trigger: () -> Unit
) : BroadcastReceiverTrigger(context) {

    companion object {
        private const val USB_STATE = "android.hardware.usb.action.USB_STATE"
        private const val USB_CONNECTED = "connected"
    }

    private var skippedFirst: Boolean = false

    override val broadcastReceiver = BroadcastReceiver {
        onAction(USB_STATE) {
            isConnected(it.extras?.getBoolean(USB_CONNECTED, false) ?: false)
        }
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
