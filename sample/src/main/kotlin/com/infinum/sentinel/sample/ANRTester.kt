package com.infinum.sentinel.sample

import android.os.Handler
import android.os.Looper
import timber.log.Timber

class ANRTester : Thread() {

    companion object {
        private const val SLEEP_DURATION = 8 * 1000L
        private const val DEADLOCK_DURATION = 1000L
        private val mutex = Any()

        fun threadSleep() {
            try {
                sleep(SLEEP_DURATION)
            } catch (e: InterruptedException) {
                Timber.tag("ANR-Sleep").e(e)
            }
        }

        fun infiniteLoop() {
            var i = 0
            while (true) {
                i++
            }
        }

        fun deadLock() {
            Handler(Looper.getMainLooper())
                .postDelayed(
                    {
                        synchronized(mutex) {
                            Timber.tag("ANR-Failed").e("There should be a dead lock before this message.")
                        }
                    },
                    DEADLOCK_DURATION
                )
            ANRTester().start()
        }
    }

    init {
        name = "ANRTester"
    }

    override fun run() =
        synchronized(mutex) {
            while (true) threadSleep()
        }
}
