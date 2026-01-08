package com.infinum.sentinel.data.sources.memory.triggers

import com.infinum.sentinel.data.models.memory.triggers.airplanemode.AirplaneModeOnTrigger
import com.infinum.sentinel.data.models.memory.triggers.foreground.ForegroundTrigger
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.data.models.memory.triggers.proximity.ProximityTrigger
import com.infinum.sentinel.data.models.memory.triggers.shake.ShakeTrigger
import com.infinum.sentinel.data.models.memory.triggers.usb.UsbConnectedTrigger
import me.tatarka.inject.annotations.Inject

@Inject
internal class TriggersCacheFactory(
    private val manual: ManualTrigger,
    private val foreground: ForegroundTrigger,
    private val shake: ShakeTrigger,
    private val proximity: ProximityTrigger,
    private val usbConnected: UsbConnectedTrigger,
    private val airplaneModeOn: AirplaneModeOnTrigger,
) : TriggersCache {
    override fun manual(): ManualTrigger = manual

    override fun foreground(): ForegroundTrigger = foreground

    override fun shake(): ShakeTrigger = shake

    override fun proximity(): ProximityTrigger = proximity

    override fun usbConnected(): UsbConnectedTrigger = usbConnected

    override fun airplaneModeOn(): AirplaneModeOnTrigger = airplaneModeOn
}
