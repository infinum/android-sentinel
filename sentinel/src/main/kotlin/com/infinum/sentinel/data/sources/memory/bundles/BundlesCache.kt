package com.infinum.sentinel.data.sources.memory.bundles

import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import kotlinx.coroutines.flow.SharedFlow

internal interface BundlesCache {

    suspend fun save(descriptor: BundleDescriptor)

    fun load(): SharedFlow<List<BundleDescriptor>>
}
