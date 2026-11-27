package com.infinum.sentinel.domain.deeplinks

import com.infinum.sentinel.domain.deeplinks.models.ExampleLinkData

/**
 * The example links of the last `DeepLinkTool` the app created. The tool sets them when it's constructed, usually in
 * `Application.onCreate`, so they're in place before the Deep links screen opens, also after process death. The
 * screen reads them once per process, when it first lists the links; a tool created later doesn't change the list.
 */
internal object ExampleLinksRegistry {
    @Volatile
    var links: List<ExampleLinkData> = listOf()
}
