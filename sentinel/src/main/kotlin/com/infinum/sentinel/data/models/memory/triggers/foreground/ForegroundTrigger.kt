package com.infinum.sentinel.data.models.memory.triggers.foreground

import com.infinum.sentinel.data.models.memory.triggers.shared.AbstractTrigger

internal class ForegroundTrigger(
    private val trigger: () -> Unit
) : AbstractTrigger() {

    override fun start() {
        if (active) {
            trigger()
        }
    }

    override fun stop() = Unit
}
