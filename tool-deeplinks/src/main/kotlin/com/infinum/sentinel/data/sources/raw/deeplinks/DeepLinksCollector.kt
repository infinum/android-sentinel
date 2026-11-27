package com.infinum.sentinel.data.sources.raw.deeplinks

import com.infinum.sentinel.data.models.raw.deeplinks.DeepLinksData
import com.infinum.sentinel.domain.deeplinks.DeepLinksCollectors
import com.infinum.sentinel.domain.deeplinks.models.ExampleLinkData

/**
 * Reads the app manifest and pairs it with the integrator's example links.
 */
internal class DeepLinksCollector(
    private val manifestReader: ManifestReader,
    private val examples: List<ExampleLinkData>,
) : DeepLinksCollectors.DeepLinks {
    override fun invoke(): DeepLinksData = DeepLinksData(manifestReader.read(), examples)
}
