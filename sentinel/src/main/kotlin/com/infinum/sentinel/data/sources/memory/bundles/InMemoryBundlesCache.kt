package com.infinum.sentinel.data.sources.memory.bundles

import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class InMemoryBundlesCache : BundlesCache {

    private val descriptors = mutableListOf<BundleDescriptor>()
    private val descriptorsFlow = MutableSharedFlow<List<BundleDescriptor>>(replay = 1)

    override suspend fun clear() {
        descriptors.clear()
        descriptorsFlow.emit(descriptors.toList())
    }

    override suspend fun save(descriptor: BundleDescriptor) {
        descriptors.add(0, descriptor)
        descriptorsFlow.emit(descriptors.toList())
    }

    override fun load(): SharedFlow<List<BundleDescriptor>> =
        descriptorsFlow.asSharedFlow()
}
