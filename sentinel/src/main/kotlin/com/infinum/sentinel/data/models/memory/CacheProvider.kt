package com.infinum.sentinel.data.models.memory

import android.content.Context
import com.infinum.sentinel.data.models.memory.triggers.TriggerProvider

internal object CacheProvider {

    private lateinit var triggers: TriggerProvider

    fun initialise(context: Context, onTriggered: () -> Unit): CacheProvider {
        triggers = TriggerProvider.initialise(context, onTriggered)

        return this
    }

    fun triggers() = triggers
}