package com.infinum.sentinel.data.models.memory.triggers.shared.samples

/** Pools samples. Avoids garbage collection.  */
internal class SamplePool {
    private var head: Sample? = null

    /** Acquires a sample from the pool.  */
    fun acquire(): Sample {
        var acquired = head
        if (acquired == null) {
            acquired = Sample()
        } else { // Remove instance from pool.
            head = acquired.next
        }
        return acquired
    }

    /** Returns a sample to the pool.  */
    fun release(sample: Sample) {
        sample.next = head
        head = sample
    }
}
