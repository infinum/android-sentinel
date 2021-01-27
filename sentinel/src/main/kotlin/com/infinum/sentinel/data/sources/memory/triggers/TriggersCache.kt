package com.infinum.sentinel.data.sources.memory.triggers

import com.infinum.sentinel.data.models.memory.triggers.airplanemode.AirplaneModeOnTrigger
import com.infinum.sentinel.data.models.memory.triggers.foreground.ForegroundTrigger
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.data.models.memory.triggers.shake.ShakeTrigger
import com.infinum.sentinel.data.models.memory.triggers.usb.UsbConnectedTrigger

internal interface TriggersCache {

    fun manual(): ManualTrigger

    fun foreground(): ForegroundTrigger

    fun shake(): ShakeTrigger

    fun usbConnected(): UsbConnectedTrigger

    fun airplaneModeOn(): AirplaneModeOnTrigger
}
