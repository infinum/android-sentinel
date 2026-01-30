package com.infinum.sentinel.ui.crash.anr

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.models.local.CrashEntity
import com.infinum.sentinel.data.models.local.crash.CrashData
import com.infinum.sentinel.data.sources.local.room.dao.CrashesDao
import com.infinum.sentinel.extensions.applicationName
import com.infinum.sentinel.extensions.asExceptionData
import com.infinum.sentinel.ui.shared.notification.NotificationFactory
import kotlinx.coroutines.runBlocking
import me.tatarka.inject.annotations.Inject

/**
 * A [Runnable] testing the UI thread every 10s until stop is called explicitly.
 */
@Inject
internal class SentinelAnrObserverRunnable(
    private val context: Context,
    private val notificationFactory: NotificationFactory,
    private val dao: CrashesDao,
) : Runnable {
    companion object {
        private const val ANR_OBSERVER_THREAD_NAME = "Sentinel-ANR-Observer"
        private const val ANR_OBSERVER_TIMEOUT: Long = 10_000
        private const val ANR_THREAD_RESPONSE_THRESHOLD: Long = 2_000
    }

    /**
     * The [Handler] to access the UI threads message queue
     */
    private val handler = Handler(Looper.getMainLooper())

    private var listener: Sentinel.ApplicationNotRespondingListener? = null

    /**
     * The stop flag
     */
    private var stopped = false

    /**
     * Flag indicating the stop was performed
     */
    @get:Synchronized
    var isStopped = true
        private set

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    override fun run() {
        Thread.currentThread().name = ANR_OBSERVER_THREAD_NAME
        isStopped = false

        while (Thread.interrupted().not()) {
            try {
                // Create new callback
                val callback = AnrObserverCallback()

                // Perform test, Handler should run the callback within 2s
                synchronized(callback) {
                    handler.post(callback)
                    (callback as Object).wait(ANR_THREAD_RESPONSE_THRESHOLD)

                    // Check if called
                    if (callback.isCalled.not()) {
                        val exception =
                            Sentinel.ApplicationNotRespondingException(
                                handler.looper.thread,
                            )

                        val entity =
                            CrashEntity(
                                applicationName = context.applicationName,
                                timestamp = System.currentTimeMillis(),
                                data =
                                    CrashData(
                                        threadState = exception.threadStateList,
                                        exception = exception.asExceptionData(isANR = true),
                                    ),
                            )
                        val id: Long = runBlocking { dao.save(entity) }
                        notificationFactory.showAnr(context.applicationName, id, entity)

                        listener?.onAppNotResponding(exception)

                        (callback as Object).wait(0L)
                    } else {
                        // Do nothing
                    }
                }

                // Check if stopped
                checkStopped()
                Thread.sleep(ANR_OBSERVER_TIMEOUT)
            } catch (e: InterruptedException) {
                break
            }
        }

        // Set stop completed flag
        isStopped = true
    }

    @Synchronized
    @Throws(InterruptedException::class)
    private fun checkStopped() {
        if (stopped) {
            Thread.sleep(ANR_THREAD_RESPONSE_THRESHOLD)
            if (stopped) {
                throw InterruptedException()
            }
        }
    }

    /**
     * Stops the check
     */
    @Synchronized
    fun stop() {
        stopped = true
    }

    /**
     * Stops the check
     */
    @Synchronized
    fun unstop() {
        stopped = false
    }

    fun setListener(listener: Sentinel.ApplicationNotRespondingListener?) {
        this.listener = listener
    }

    /**
     * Runnable as callback that calls notifyAll on run.
     */
    internal class AnrObserverCallback : Runnable {
        @get:Synchronized
        var isCalled = false
            private set

        @Synchronized
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        override fun run() {
            isCalled = true
            (this as Object).notifyAll()
        }
    }
}
