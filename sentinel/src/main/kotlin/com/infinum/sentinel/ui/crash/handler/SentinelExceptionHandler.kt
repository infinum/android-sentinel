package com.infinum.sentinel.ui.crash.handler

internal interface SentinelExceptionHandler : Thread.UncaughtExceptionHandler {

    fun startCatchingUncaughtExceptions()

    fun stopCatchingUncaughtExceptions()

    fun setExceptionHandler(handler: Thread.UncaughtExceptionHandler?)
}
