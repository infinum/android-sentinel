package com.infinum.sentinel.ui.crash.handler

internal interface SentinelExceptionHandler : Thread.UncaughtExceptionHandler {
    fun start()

    fun stop()

    fun setExceptionHandler(handler: Thread.UncaughtExceptionHandler?)
}
