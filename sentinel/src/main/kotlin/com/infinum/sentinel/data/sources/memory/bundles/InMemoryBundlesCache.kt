package com.infinum.sentinel.data.sources.memory.bundles

import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor

internal class InMemoryBundlesCache : BundlesCache {

    private val descriptors = mutableListOf<BundleDescriptor>()

    override fun save(descriptor: BundleDescriptor) {
        descriptors.add(0, descriptor)
    }

    override fun load(): List<BundleDescriptor> =
        descriptors
}
