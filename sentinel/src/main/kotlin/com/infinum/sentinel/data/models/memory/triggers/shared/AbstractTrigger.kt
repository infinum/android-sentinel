package com.infinum.sentinel.data.models.memory.triggers.shared

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.infinum.sentinel.data.models.memory.triggers.Trigger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal abstract class AbstractTrigger : LifecycleObserver, Trigger {

    internal var active: Boolean = false

    init {
        GlobalScope.launch(Dispatchers.Main) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this@AbstractTrigger)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onForeground() = start()

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onBackground() = stop()
}
