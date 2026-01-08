package com.infinum.sentinel.domain.crash.monitor.models

import com.infinum.sentinel.data.models.local.CrashMonitorEntity
import com.infinum.sentinel.domain.shared.base.BaseParameters

internal data class CrashMonitorParameters(
    val entity: CrashMonitorEntity? = null,
) : BaseParameters
