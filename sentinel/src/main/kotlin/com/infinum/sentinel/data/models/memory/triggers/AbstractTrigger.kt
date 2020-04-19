package com.infinum.sentinel.data.models.memory.triggers

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

abstract class AbstractTrigger: LifecycleObserver, Trigger {

    internal var active: Boolean = false

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onForeground() = start()

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onBackground() = stop()
}