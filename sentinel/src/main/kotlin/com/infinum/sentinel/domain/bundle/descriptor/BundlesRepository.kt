package com.infinum.sentinel.domain.bundle.descriptor

import com.infinum.sentinel.data.sources.memory.bundles.BundlesCache
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
internal class BundlesRepository(
    private val cache: BundlesCache,
) : Repositories.Bundles {
    override suspend fun clear() = cache.clear()

    override suspend fun save(input: BundleParameters) {
        input.descriptor?.let { cache.save(it) }
            ?: error("Cannot save null descriptors")
    }

    override fun load(input: BundleParameters): Flow<List<BundleDescriptor>> = cache.load()
}
