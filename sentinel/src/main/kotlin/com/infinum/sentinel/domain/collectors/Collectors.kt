package com.infinum.sentinel.domain.collectors

import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.models.raw.ApplicationData
import com.infinum.sentinel.data.models.raw.DeviceData
import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.data.sources.raw.collectors.Collector

internal interface Collectors {

    interface Device : Collector<DeviceData>

    interface Application : Collector<ApplicationData>

    interface Permissions : Collector<Map<String, Boolean>>

    interface Preferences : Collector<List<PreferencesData>>

    interface Tools : Collector<Set<Sentinel.Tool>>
}