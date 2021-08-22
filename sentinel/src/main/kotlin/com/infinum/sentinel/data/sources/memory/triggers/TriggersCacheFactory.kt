package com.infinum.sentinel.data.sources.memory.triggers

import com.infinum.sentinel.data.models.memory.triggers.airplanemode.AirplaneModeOnTrigger
import com.infinum.sentinel.data.models.memory.triggers.foreground.ForegroundTrigger
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.data.models.memory.triggers.shake.ShakeTrigger
import com.infinum.sentinel.data.models.memory.triggers.usb.UsbConnectedTrigger

internal class TriggersCacheFactory(
    private val manual: ManualTrigger,
    private val foreground: ForegroundTrigger,
    private val shake: ShakeTrigger,
    private val usbConnected: UsbConnectedTrigger,
    private val airplaneModeOn: AirplaneModeOnTrigger
) : TriggersCache {

    override fun manual(): ManualTrigger = manual

    override fun foreground(): ForegroundTrigger = foreground

    override fun shake(): ShakeTrigger = shake

    override fun usbConnected(): UsbConnectedTrigger = usbConnected

    override fun airplaneModeOn(): AirplaneModeOnTrigger = airplaneModeOn
}
