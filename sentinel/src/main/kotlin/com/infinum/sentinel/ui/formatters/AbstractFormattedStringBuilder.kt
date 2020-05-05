package com.infinum.sentinel.ui.formatters

import android.content.Context
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.PreferencesCollector

internal abstract class AbstractFormattedStringBuilder<T, K>(
    context: Context
) : FormattedStringBuilder<T, K> {

    companion object {
        internal const val APPLICATION = "application"
        internal const val DEVICE = "device"
        internal const val PERMISSIONS = "permissions"
        internal const val NAME = "name"
        internal const val STATUS = "status"
        internal const val PREFERENCES = "preferences"
        internal const val VALUES = "values"
    }

    internal val applicationCollector: ApplicationCollector = ApplicationCollector(context)
    internal val permissionsCollector: PermissionsCollector = PermissionsCollector(context)
    internal val deviceCollector: DeviceCollector = DeviceCollector()
    internal val preferencesCollector: PreferencesCollector = PreferencesCollector(context)

    init {
        applicationCollector.collect()
        permissionsCollector.collect()
        deviceCollector.collect()
        preferencesCollector.collect()
    }
}
