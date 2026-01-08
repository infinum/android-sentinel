package com.infinum.sentinel.extensions

import com.infinum.sentinel.data.models.local.crash.ThreadGroupData

internal fun ThreadGroup?.asThreadGroupData(): ThreadGroupData? =
    this?.let {
        ThreadGroupData(
            name = it.name,
            parent = it.parent.name,
            activeCount = it.activeCount(),
        )
    }
