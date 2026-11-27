package com.infinum.sentinel.ui.deeplinks.breakdown

import com.infinum.sentinel.domain.deeplinks.models.QueryParam

/**
 * A link split into the parts the breakdown screen shows.
 */
internal sealed interface LinkBreakdown {
    /** The link exactly as it was passed in. */
    val fullLink: String

    /**
     * A link that could be split. [path] and [fragment] are decoded, [params] are decoded plain text.
     */
    data class Parsed(
        override val fullLink: String,
        val scheme: String,
        val host: String?,
        val port: Int?,
        val path: String,
        val params: List<QueryParam>,
        val fragment: String?,
    ) : LinkBreakdown

    /** A link without a scheme, which cannot be split. */
    data class Unparseable(
        override val fullLink: String,
    ) : LinkBreakdown
}
