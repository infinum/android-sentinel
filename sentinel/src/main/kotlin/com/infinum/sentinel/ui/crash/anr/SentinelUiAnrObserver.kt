package com.infinum.sentinel.ui.crash.anr

import com.infinum.sentinel.Sentinel
import java.util.concurrent.ExecutorService
import me.tatarka.inject.annotations.Inject

/**
 * A class observing the UI thread for ANR errors.
 */
@Inject
internal class SentinelUiAnrObserver(
    private val observerRunnable: SentinelAnrObserverRunnable,
    private val executor: ExecutorService
) : SentinelAnrObserver {
    /**
     * Start observing.
     */
    @Synchronized
    override fun start() =
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
    override fun stop() =
        observerRunnable.stop()

    override fun setListener(listener: Sentinel.ApplicationNotRespondingListener?) {
        observerRunnable.setListener(listener)
    }
}
