package com.infinum.sentinel.extensions

import com.infinum.sentinel.data.models.local.crash.ThreadData

internal fun Thread.asThreadData(): ThreadData =
    ThreadData(
        id = id,
        name = name,
        isDaemon = isDaemon,
        state = state.name,
        group = threadGroup.asThreadGroupData(),
        priority = priority,
    )
