package com.infinum.sentinel.ui.crash.anr

import com.infinum.sentinel.Sentinel

internal interface SentinelAnrObserver {

    fun start()

    fun stop()

    fun setListener(listener: Sentinel.ApplicationNotRespondingListener?)
}
