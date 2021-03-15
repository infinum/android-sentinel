package com.infinum.sentinel.domain.bundle.shared

import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import com.infinum.sentinel.domain.shared.base.BaseParameters

internal data class BundlesParameters(
    val monitor: BundleMonitorParameters,
    val details: BundleParameters
) : BaseParameters
