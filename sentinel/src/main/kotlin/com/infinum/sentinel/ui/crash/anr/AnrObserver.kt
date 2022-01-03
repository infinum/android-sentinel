package com.infinum.sentinel.ui.crash.anr

import com.infinum.sentinel.Sentinel
import java.util.concurrent.ExecutorService

/**
 * A class observing the UI thread for ANR errors.
 */
internal class AnrObserver(
    private val observerRunnable: AnrObserverRunnable,
    private val executor: ExecutorService
) {
    /**
     * Start observing.
     */
    @Synchronized
    fun start() =
        synchronized(observerRunnable) {
            if (observerRunnable.isStopped) {
                executor.execute(observerRunnable)
            } else {
                observerRunnable.unstop()
            }
        }

    /**
     * Stop observing.
     * The stop is delayed, so if start() is called right after stop(),
     * both methods will have no effect.
     * There will be at least one more ANR check before the observing is stopped.
     */
    @Synchronized
    fun stop() =
        observerRunnable.stop()

    fun setListener(listener: Sentinel.ApplicationNotRespondingListener?) {
        observerRunnable.setListener(listener)
    }
}
