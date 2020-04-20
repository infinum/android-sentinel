package com.infinum.sentinel.data.models.memory.triggers.foreground

import androidx.lifecycle.ProcessLifecycleOwner
import com.infinum.sentinel.data.models.memory.triggers.AbstractTrigger

class ForegroundTrigger(
    private val trigger: () -> Unit
) : AbstractTrigger() {

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun start() {
        if (active) {
            trigger()
        }
    }

    override fun stop() = Unit
}
