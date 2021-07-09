package com.infinum.sentinel.data.models.memory.triggers.shake

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.infinum.sentinel.data.models.memory.triggers.shake.samples.SampleQueue
import com.infinum.sentinel.data.models.memory.triggers.shared.AbstractTrigger

/**
 * Detects phone shaking. If more than 75% of the samples taken in the past 0.5s are
 * accelerating, the device is a) shaking, or b) free falling 1.84m (h =
 * 1/2*g*t^2*3/4).
 *
 */
internal class ShakeTrigger(
    private val context: Context,
    private val trigger: () -> Unit
) : AbstractTrigger(), SensorEventListener {

    companion object {
        private const val MAGNITUDE_THRESHOLD = 169
    }

    private val queue = SampleQueue()
    private var sensorManager: SensorManager? = null

    /**
     * Starts listening for shakes on devices with appropriate hardware.
     */
    override fun start() {
        sensorManager = (context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager)
        sensorManager?.let {
            registerSensor(it)
        } ?: run {
            this.active = false
        }
    }

    /**
     * Stops listening.  Safe to call when already stopped.  Ignored on devices
     * without appropriate hardware.
     */
    override fun stop() {
        queue.clear()
        unregisterSensor()
        sensorManager = null
        this.active = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        val accelerating = isAccelerating(event)
        val timestamp = event.timestamp
        queue.add(timestamp, accelerating)
        if (queue.isShaking) {
            queue.clear()
            if (active) {
                trigger()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    private fun registerSensor(sensorManager: SensorManager) {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            this.active = true
        } ?: run {
            this.active = false
        }
    }

    private fun unregisterSensor() {
        sensorManager?.unregisterListener(this)
    }

    /** Returns true if the device is currently accelerating.  */
    private fun isAccelerating(event: SensorEvent): Boolean {
        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]
        // Instead of comparing magnitude to ACCELERATION_THRESHOLD,
        // compare their squares. This is equivalent and doesn't need the
        // actual magnitude, which would be computed using (expensive) Math.sqrt().
        val magnitudeSquared = ax * ax + ay * ay + (az * az).toDouble()
        return magnitudeSquared > MAGNITUDE_THRESHOLD
    }
}
