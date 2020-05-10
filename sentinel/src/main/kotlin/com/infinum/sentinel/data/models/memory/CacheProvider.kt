package com.infinum.sentinel.data.models.memory

import android.content.Context
import com.infinum.sentinel.data.models.memory.triggers.TriggerProvider

internal object CacheProvider {

    private lateinit var triggers: TriggerProvider

    fun initialise(context: Context): CacheProvider {
        triggers = TriggerProvider.initialise(context)

        return this
    }

    fun setup(onTriggered: () -> Unit) {
        TriggerProvider.setup(onTriggered)
    }

    fun triggers() = triggers
}
