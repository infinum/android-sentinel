package com.infinum.sentinel.data.models.memory.triggers.shake.samples

/** Queue of samples. Keeps a running average.  */
internal class SampleQueue {

    companion object {
        /** Window size in ns. Used to compute the average.  */
        private const val MAX_WINDOW_SIZE: Long = 500000000 // 0.5s
        private const val MIN_WINDOW_SIZE = MAX_WINDOW_SIZE shr 1 // 0.25s
        /**
         * Ensure the queue size never falls below this size, even if the device
         * fails to deliver this many events during the time window. The LG Ally
         * is one such device.
         */
        private const val MIN_QUEUE_SIZE = 4
    }

    private val pool = SamplePool()
    private var oldest: Sample? = null
    private var newest: Sample? = null
    private var sampleCount = 0
    private var acceleratingCount = 0

    /**
     * Adds a sample.
     *
     * @param timestamp in nanoseconds of sample
     * @param accelerating true if > [.accelerationThreshold].
     */
    fun add(timestamp: Long, accelerating: Boolean) { // Purge samples that proceed window.
        purge(timestamp - MAX_WINDOW_SIZE)

        // Add the sample to the queue.
        val added = pool.acquire().apply {
            this.timestamp = timestamp
            this.accelerating = accelerating
            this.next = null
        }

        newest?.let {
            it.next = added
        }
        newest = added

        if (oldest == null) {
            oldest = added
        }

        // Update running average.
        sampleCount++
        if (accelerating) {
            acceleratingCount++
        }
    }

    /** Removes all samples from this queue.  */
    fun clear() {
        while (oldest != null) {
            val removed: Sample = oldest as Sample
            oldest = removed.next
            pool.release(removed)
        }
        newest = null
        sampleCount = 0
        acceleratingCount = 0
    }

    /** Purges samples with timestamps older than cutoff.  */
    private fun purge(cutoff: Long) {
        while (
            sampleCount >= MIN_QUEUE_SIZE &&
            oldest != null &&
            (cutoff - (oldest?.timestamp ?: 0)) > 0
        ) { // Remove sample.
            val removed: Sample = oldest as Sample
            if (removed.accelerating) {
                acceleratingCount--
            }
            sampleCount--
            oldest = removed.next
            if (oldest == null) {
                newest = null
            }
            pool.release(removed)
        }
    }

    /**
     * Returns true if we have enough samples and more than 3/4 of those samples
     * are accelerating.
     */
    val isShaking: Boolean
        get() = newest != null &&
                oldest != null &&
                ((newest?.timestamp ?: 0) - (oldest?.timestamp ?: 0)) >= MIN_WINDOW_SIZE &&
                acceleratingCount >= (sampleCount shr 1) + (sampleCount shr 2)
}
