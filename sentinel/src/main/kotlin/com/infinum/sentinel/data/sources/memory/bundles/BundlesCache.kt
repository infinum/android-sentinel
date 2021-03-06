package com.infinum.sentinel.data.sources.memory.bundles

import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import kotlinx.coroutines.flow.Flow

internal interface BundlesCache {

    fun save(descriptor: BundleDescriptor)

    fun load(): Flow<BundleDescriptor>
}