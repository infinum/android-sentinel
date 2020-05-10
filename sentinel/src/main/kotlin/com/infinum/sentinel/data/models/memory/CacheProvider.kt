package com.infinum.sentinel.data.models.memory

import android.content.Context
import com.infinum.sentinel.data.models.memory.triggers.TriggerProvider

internal class CacheProvider(context: Context) {

    private val triggers: TriggerProvider = TriggerProvider(context)

    fun setup(onTriggered: () -> Unit) = triggers.setup(onTriggered)

    fun triggers() = triggers
}
