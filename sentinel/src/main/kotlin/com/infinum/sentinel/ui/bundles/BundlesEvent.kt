package com.infinum.sentinel.ui.bundles

import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor

internal sealed class BundlesEvent {
    data class BundlesIntercepted(
        val value: List<BundleDescriptor>,
    ) : BundlesEvent()
}
