package com.infinum.sentinel.domain.bundle.monitor.models

import com.infinum.sentinel.data.models.local.BundleMonitorEntity
import com.infinum.sentinel.domain.shared.base.BaseParameters

internal data class BundleMonitorParameters(
    val query: String? = null,
    val entity: BundleMonitorEntity? = null
) : BaseParameters
