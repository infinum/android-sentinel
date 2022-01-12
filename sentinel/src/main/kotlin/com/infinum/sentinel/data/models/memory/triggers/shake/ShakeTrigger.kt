package com.infinum.sentinel.data.models.memory.triggers.shake

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import com.infinum.sentinel.data.models.memory.triggers.shared.SensorTrigger
import com.infinum.sentinel.data.models.memory.triggers.shared.samples.SampleQueue

internal class ShakeTrigger(
    context: Context,
    trigger: () -> Unit
) : SensorTrigger(context, trigger, Sensor.TYPE_ACCELEROMETER, SampleQueue()) {

    companion object {
        private const val MAGNITUDE_THRESHOLD = 169
    }

    override fun threshold(): Int = MAGNITUDE_THRESHOLD

    override fun processEvent(event: SensorEvent): Boolean {
        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]
        val magnitudeSquared = ax * ax + ay * ay + (az * az).toDouble()
        return magnitudeSquared > threshold()
    }
}
