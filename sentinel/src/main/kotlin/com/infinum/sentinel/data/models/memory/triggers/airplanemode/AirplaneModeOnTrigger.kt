package com.infinum.sentinel.data.models.memory.triggers.airplanemode

import android.content.Context
import android.content.Intent
import com.infinum.sentinel.data.models.memory.triggers.shared.BroadcastReceiverTrigger
import com.infinum.sentinel.data.models.memory.triggers.shared.receiver.BroadcastReceiver
import me.tatarka.inject.annotations.Inject

@Inject
internal class AirplaneModeOnTrigger(
    context: Context,
    private val trigger: () -> Unit
) : BroadcastReceiverTrigger(context) {

    companion object {
        private const val STATE = "state"
    }

    override val broadcastReceiver: BroadcastReceiver = BroadcastReceiver {
        onAction(Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            if (active) {
                if (it.hasExtra(STATE)) {
                    val isEnabled = it.getBooleanExtra(STATE, false)
                    if (isEnabled) {
                        trigger()
                    }
                }
            }
        }
    }
}
