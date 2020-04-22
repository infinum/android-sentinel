package com.infinum.sentinel.data.models.memory.triggers.manual

import com.infinum.sentinel.data.models.memory.triggers.AbstractTrigger

internal class ManualTrigger : AbstractTrigger() {

    override fun start() = Unit

    override fun stop() = Unit
}
