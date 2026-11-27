package com.infinum.sentinel.domain.deeplinks.discovery

import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.ExampleLinkData
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.domain.deeplinks.uri.DeepLinkUriEncoder
import com.infinum.sentinel.domain.deeplinks.uri.DeepLinkUriParser

/**
 * Turns the integrator's example links into base links.
 */
internal object ExampleLinkMapper {
    /**
     * Maps each example to a [BaseLink]. Examples with the same text are merged, the first one wins.
     * An example that cannot be parsed is kept with a null spec so the user still sees it.
     */
    internal fun map(examples: List<ExampleLinkData>): List<BaseLink> =
        examples
            .distinctBy { BaseLinkKeys.example(it.uri) }
            .map { example ->
                val parsed = DeepLinkUriParser.parse(example.uri)
                BaseLink(
                    key = BaseLinkKeys.example(example.uri),
                    origin = BaseLinkOrigin.Example(label = example.label, rawUri = example.uri),
                    spec =
                    parsed?.let {
                        LinkSpec(
                            scheme = it.scheme,
                            host = it.host?.let(HostSpec::Literal) ?: HostSpec.None,
                            port = it.port,
                            path =
                            if (it.path.isEmpty()) {
                                PathSpec.None
                            } else {
                                PathSpec.Literal(DeepLinkUriEncoder.decodePath(it.path))
                            },
                        )
                    },
                    exampleUri = parsed,
                )
            }
}
