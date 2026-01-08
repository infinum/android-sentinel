package com.infinum.sentinel.data.models.memory.triggers.usb

import android.content.Context
import com.infinum.sentinel.data.models.memory.triggers.shared.BroadcastReceiverTrigger
import com.infinum.sentinel.data.models.memory.triggers.shared.receiver.BroadcastReceiver
import me.tatarka.inject.annotations.Inject

@Inject
internal class UsbConnectedTrigger(
    private val context: Context,
    private val trigger: () -> Unit,
) : BroadcastReceiverTrigger(context) {
    companion object {
        private const val USB_STATE = "android.hardware.usb.action.USB_STATE"
        private const val USB_CONNECTED = "connected"
    }

    override val broadcastReceiver =
        BroadcastReceiver {
            onAction(USB_STATE) {
                if (active) {
                    if (it.hasExtra(USB_CONNECTED)) {
                        val isConnected = it.getBooleanExtra(USB_CONNECTED, false)
                        if (isConnected) {
                            trigger()
                        }
                    }
                }
            }
        }
}
