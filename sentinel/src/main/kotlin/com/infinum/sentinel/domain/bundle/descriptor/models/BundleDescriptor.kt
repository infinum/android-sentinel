package com.infinum.sentinel.domain.bundle.descriptor.models

import com.infinum.sentinel.data.models.memory.bundles.BundleTree
import com.infinum.sentinel.ui.bundles.callbacks.BundleCallSite

internal data class BundleDescriptor(
    val timestamp: Long,
    val className: String?,
    val callSite: BundleCallSite,
    val bundleTree: BundleTree,
    val limit: Int = 0
)
