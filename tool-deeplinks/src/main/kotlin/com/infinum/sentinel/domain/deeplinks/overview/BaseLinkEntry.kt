package com.infinum.sentinel.domain.deeplinks.overview

import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus

/**
 * A base link with the variants attached to it.
 *
 * @property baseLink the declared or example link.
 * @property variants the variants shown under the link, in storage order.
 * @property status the App Links verification status of the link's host. Null when it has none.
 */
internal data class BaseLinkEntry(
    val baseLink: BaseLink,
    val variants: List<VariantEntry>,
    val status: VerificationStatus? = null,
) {
    /** How many variants the link has. */
    val variantCount: Int
        get() = variants.size
}
