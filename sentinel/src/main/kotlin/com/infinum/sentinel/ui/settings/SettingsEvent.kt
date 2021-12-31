package com.infinum.sentinel.ui.settings

import com.infinum.sentinel.data.models.local.BundleMonitorEntity
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity

internal sealed class SettingsEvent {

    data class TriggersChanged(
        val value: List<TriggerEntity>
    ) : SettingsEvent()

    data class FormatChanged(
        val value: FormatEntity
    ) : SettingsEvent()

    data class BundleMonitorChanged(
        val value: BundleMonitorEntity
    ) : SettingsEvent()
}
