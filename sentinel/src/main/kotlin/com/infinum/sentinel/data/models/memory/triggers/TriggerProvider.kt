package com.infinum.sentinel.data.models.memory.triggers

import android.content.Context
import com.infinum.sentinel.data.models.memory.triggers.airplanemode.AirplaneModeOnTrigger
import com.infinum.sentinel.data.models.memory.triggers.foreground.ForegroundTrigger
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.data.models.memory.triggers.shake.ShakeTrigger
import com.infinum.sentinel.data.models.memory.triggers.usb.UsbConnectedTrigger

internal class TriggerProvider(private val context: Context) {

    private val manual: ManualTrigger = ManualTrigger()

    private lateinit var foreground: ForegroundTrigger

    private lateinit var shake: ShakeTrigger

    private lateinit var usbConnected: UsbConnectedTrigger

    private lateinit var airplaneModeOn: AirplaneModeOnTrigger

    fun setup(onTriggered: () -> Unit) {
        foreground = ForegroundTrigger(onTriggered)
        shake = ShakeTrigger(context, onTriggered)
        usbConnected = UsbConnectedTrigger(context, onTriggered)
        airplaneModeOn = AirplaneModeOnTrigger(context, onTriggered)
    }

    fun manual() = manual

    fun foreground() = foreground

    fun shake() = shake

    fun usbConnected() = usbConnected

    fun airplaneModeOn() = airplaneModeOn
}
