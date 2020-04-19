package com.infinum.sentinel.data.models.memory.triggers.shared.receiver

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver as AndroidBroadcastReceiver

class BroadcastReceiver(
    builder: Builder.() -> Unit
) {
    private val instructions: List<Instructions>

    val filter: IntentFilter

    init {
        with(Builder()) {
            builder(this)
            filter = filter()
            instructions = instructions()
        }
    }

    val broadcastReceiver = object : AndroidBroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            for (instruction in instructions) {
                if (instruction.matches(intent)) {
                    instruction.execution().invoke(intent)
                    break
                }
            }
        }
    }
}