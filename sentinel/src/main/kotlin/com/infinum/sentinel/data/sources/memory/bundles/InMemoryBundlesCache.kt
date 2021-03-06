package com.infinum.sentinel.data.sources.memory.bundles

import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

internal class InMemoryBundlesCache : BundlesCache {

    private val descriptors = mutableListOf<BundleDescriptor>()

    override fun save(descriptor: BundleDescriptor) =
        descriptors.add(0, descriptor)

    override fun load(): Flow<BundleDescriptor> =
        descriptors.asFlow()
}