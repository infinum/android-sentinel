package com.infinum.sentinel.domain.deeplinks.overview

/**
 * Everything the Deep links list shows, in display order. Built by [DeepLinksOverviewAssembler].
 *
 * @property notices the notices shown above every section.
 * @property sections the sections, in display order.
 * @property isEmpty true when discovery worked and there are no example links, declared screens or saved variants.
 */
internal data class DeepLinksOverview(
    val notices: List<DeepLinksNotice>,
    val sections: List<DeepLinksSection>,
    val isEmpty: Boolean,
)
