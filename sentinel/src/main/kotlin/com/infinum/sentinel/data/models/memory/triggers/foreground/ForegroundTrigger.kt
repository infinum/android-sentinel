package com.infinum.sentinel.data.models.memory.triggers.foreground

import androidx.lifecycle.ProcessLifecycleOwner
import com.infinum.sentinel.data.models.memory.triggers.AbstractTrigger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class ForegroundTrigger(
    private val trigger: () -> Unit
) : AbstractTrigger() {

    init {
        GlobalScope.launch(Dispatchers.Main) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this@ForegroundTrigger)
        }
    }

    override fun start() {
        if (active) {
            trigger()
        }
    }

    override fun stop() = Unit
}
