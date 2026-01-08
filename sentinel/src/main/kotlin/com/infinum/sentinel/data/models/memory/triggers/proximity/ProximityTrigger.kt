package com.infinum.sentinel.data.models.memory.triggers.proximity

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import com.infinum.sentinel.data.models.memory.triggers.shared.SensorTrigger
import me.tatarka.inject.annotations.Inject

@Inject
internal class ProximityTrigger(
    context: Context,
    trigger: () -> Unit,
) : SensorTrigger(context, trigger, Sensor.TYPE_PROXIMITY, null) {
    companion object {
        private const val SENSITIVITY_THRESHOLD = 4
    }

    override fun threshold(): Int = SENSITIVITY_THRESHOLD

    override fun processEvent(event: SensorEvent): Boolean {
        val value = event.values[0]
        return value >= -threshold() && value <= threshold()
    }
}
