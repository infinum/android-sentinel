package com.infinum.sentinel.data.models.memory.triggers.shared.samples

/** An accelerometer sample.  */
internal data class Sample(
    /** Time sample was taken.  */
    var timestamp: Long = 0,
    /** If event values > threshold().  */
    var triggered: Boolean = false,
    /** Next sample in the queue or pool.  */
    var next: Sample? = null,
)
