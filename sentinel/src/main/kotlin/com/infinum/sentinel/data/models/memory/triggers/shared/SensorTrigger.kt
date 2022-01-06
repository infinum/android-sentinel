package com.infinum.sentinel.data.models.memory.triggers.shared

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.infinum.sentinel.data.models.memory.triggers.shared.samples.SampleQueue

internal abstract class SensorTrigger(
    private val context: Context,
    private val trigger: () -> Unit,
    private val sensorType: Int
) : AbstractTrigger(), SensorEventListener {

    private val queue = SampleQueue()
    private var sensorManager: SensorManager? = null

    abstract fun threshold(): Int

    abstract fun processEvent(event: SensorEvent): Boolean

    override fun start() {
        sensorManager = (context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager)
        sensorManager?.let {
            registerSensor(it)
        } ?: run {
            this.active = false
        }
    }

    override fun stop() {
        queue.clear()
        unregisterSensor()
        sensorManager = null
        this.active = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        val triggered = processEvent(event)
        val timestamp = event.timestamp
        queue.add(timestamp, triggered)
        if (queue.isTriggered) {
            queue.clear()
            if (active) {
                trigger()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    private fun registerSensor(sensorManager: SensorManager) {
        sensorManager.getDefaultSensor(sensorType)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            this.active = true
        } ?: run {
            this.active = false
        }
    }

    private fun unregisterSensor() {
        sensorManager?.unregisterListener(this)
    }
}
