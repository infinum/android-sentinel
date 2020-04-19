package com.infinum.sentinel.data.models.memory.triggers.shake.samples

/** An accelerometer sample.  */
internal data class Sample(
    /** Time sample was taken.  */
    var timestamp: Long = 0,

    /** If acceleration > [.accelerationThreshold].  */
    var accelerating: Boolean = false,

    /** Next sample in the queue or pool.  */
    var next: Sample? = null
)