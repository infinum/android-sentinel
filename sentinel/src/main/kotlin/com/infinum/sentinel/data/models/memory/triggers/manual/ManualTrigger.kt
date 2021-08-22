package com.infinum.sentinel.data.models.memory.triggers.manual

import com.infinum.sentinel.data.models.memory.triggers.shared.AbstractTrigger

internal class ManualTrigger : AbstractTrigger() {

    init {
        active = true
    }

    override fun start() = Unit

    override fun stop() = Unit
}
