package com.infinum.sentinel.data.models.memory.triggers.shared.samples

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
    private var constrainedCount = 0

    /**
     * Adds a sample.
     *
     * @param timestamp in nanoseconds of sample
     * @param triggered true if > threshold().
     */
    fun add(
        timestamp: Long,
        triggered: Boolean,
    ) { // Purge samples that proceed window.
        purge(timestamp - MAX_WINDOW_SIZE)

        // Add the sample to the queue.
        val added =
            pool.acquire().apply {
                this.timestamp = timestamp
                this.triggered = triggered
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
        if (triggered) {
            constrainedCount++
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
        constrainedCount = 0
    }

    /** Purges samples with timestamps older than cutoff.  */
    private fun purge(cutoff: Long) {
        while (
            sampleCount >= MIN_QUEUE_SIZE &&
            oldest != null &&
            (cutoff - (oldest?.timestamp ?: 0)) > 0
        ) { // Remove sample.
            val removed: Sample = oldest as Sample
            if (removed.triggered) {
                constrainedCount--
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
     * are in requirement parameters.
     */
    val isTriggered: Boolean
        get() =
            if (newest != null && oldest != null) {
                hasWindowPassed(newest!!.timestamp, oldest!!.timestamp) && hasCountPassed()
            } else {
                false
            }

    private fun hasWindowPassed(
        newest: Long,
        oldest: Long,
    ) = newest - oldest >= MIN_WINDOW_SIZE

    private fun hasCountPassed() = constrainedCount >= (sampleCount shr 1) + (sampleCount shr 2)
}
