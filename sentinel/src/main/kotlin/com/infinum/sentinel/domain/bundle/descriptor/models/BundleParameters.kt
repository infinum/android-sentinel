package com.infinum.sentinel.domain.bundle.descriptor.models

import com.infinum.sentinel.domain.shared.base.BaseParameters

internal data class BundleParameters(
    val bundleId: String? = null,
    val descriptor: BundleDescriptor? = null,
) : BaseParameters
