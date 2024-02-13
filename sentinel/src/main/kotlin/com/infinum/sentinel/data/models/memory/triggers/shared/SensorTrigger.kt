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
    private val sensorType: Int,
    private val queue: SampleQueue?
) : AbstractTrigger(), SensorEventListener {

    private var sensorManager: SensorManager? = null

    abstract fun threshold(): Int

    abstract fun processEvent(event: SensorEvent): Boolean

    private var activeHolder: Boolean? = null

    override fun start() {
        sensorManager = (context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager)
        sensorManager?.let {
            registerSensor(it)
            if (activeHolder == null) {
                activeHolder = active
            }
            this.active = activeHolder ?: true // use remembered active state
        } ?: run {
            this.active = false
        }
    }

    override fun stop() {
        queue?.clear()
        unregisterSensor()
        sensorManager = null
        activeHolder = active // remember active state
        this.active = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        val triggered = processEvent(event)
        val timestamp = event.timestamp
        queue?.let {
            it.add(timestamp, triggered)
            if (it.isTriggered) {
                it.clear()
                if (active) {
                    trigger()
                }
            }
        } ?: run {
            if (triggered) {
                if (active) {
                    trigger()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    private fun registerSensor(sensorManager: SensorManager) {
        sensorManager.getDefaultSensor(sensorType)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: run {
            this.active = false
        }
    }

    private fun unregisterSensor() {
        sensorManager?.unregisterListener(this)
    }
}
