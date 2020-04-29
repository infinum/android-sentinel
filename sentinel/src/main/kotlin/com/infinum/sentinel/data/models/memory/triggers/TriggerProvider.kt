package com.infinum.sentinel.data.models.memory.triggers

import android.content.Context
import com.infinum.sentinel.data.models.memory.triggers.airplanemode.AirplaneModeOnTrigger
import com.infinum.sentinel.data.models.memory.triggers.foreground.ForegroundTrigger
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.data.models.memory.triggers.shake.ShakeTrigger
import com.infinum.sentinel.data.models.memory.triggers.usb.UsbConnectedTrigger

internal object TriggerProvider {

    private lateinit var manual: ManualTrigger

    private lateinit var foreground: ForegroundTrigger

    private lateinit var shake: ShakeTrigger

    private lateinit var usbConnected: UsbConnectedTrigger

    private lateinit var airplaneModeOn: AirplaneModeOnTrigger

    fun initialise(context: Context, onTriggered: () -> Unit): TriggerProvider {
        manual = ManualTrigger()
        foreground = ForegroundTrigger(onTriggered)
        shake = ShakeTrigger(context, onTriggered)
        usbConnected = UsbConnectedTrigger(context, onTriggered)
        airplaneModeOn = AirplaneModeOnTrigger(context, onTriggered)

        return this
    }

    fun manual() = manual

    fun foreground() = foreground

    fun shake() = shake

    fun usbConnected() = usbConnected

    fun airplaneModeOn() = airplaneModeOn
}
