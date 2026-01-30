package com.infinum.sentinel.ui.bundles.details

import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor

internal sealed class BundleDetailsState {
    data class Data(
        val value: BundleDescriptor,
    ) : BundleDetailsState()
}
