package com.infinum.sentinel.domain.bundle.descriptor

import com.infinum.sentinel.data.sources.memory.bundles.BundlesCache
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import kotlinx.coroutines.flow.Flow

internal class BundlesRepository(
    private val cache: BundlesCache
) : Repositories.Bundles {

    override suspend fun save(input: BundleParameters) {
        input.descriptor?.let { cache.save(it) }
            ?: throw IllegalStateException("Cannot save null descriptors")
    }

    override suspend fun load(input: BundleParameters): Flow<BundleDescriptor> =
        cache.load()
}
