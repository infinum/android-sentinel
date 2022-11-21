package com.infinum.sentinel.data.models.memory.triggers.shared

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.infinum.sentinel.data.models.memory.triggers.shared.samples.SampleQueue
import timber.log.Timber

internal abstract class SensorTrigger(
    private val context: Context,
    private val trigger: () -> Unit,
    private val sensorType: Int,
    private val queue: SampleQueue?
) : AbstractTrigger(), SensorEventListener {

    private var sensorManager: SensorManager? = null

    abstract fun threshold(): Int

    abstract fun processEvent(event: SensorEvent): Boolean

    override fun start() {
        sensorManager = (context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager)
        sensorManager?.let {
            registerSensor(it)
            this.active = true
        } ?: run {
            this.active = false
        }
    }

    override fun stop() {
        queue?.clear()
        unregisterSensor()
        sensorManager = null
        this.active = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        val triggered = processEvent(event)
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
//            Timber.tag("_BOJAN_").i("SensorTrigger -> onSensorChanged -> ${event.values[0]}")
        }
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
