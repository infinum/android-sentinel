package com.infinum.sentinel.ui.crash.anr

/**
 * A [Runnable] which calls [.notifyAll] when run.
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
