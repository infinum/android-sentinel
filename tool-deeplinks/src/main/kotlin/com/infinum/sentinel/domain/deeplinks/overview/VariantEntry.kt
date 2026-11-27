package com.infinum.sentinel.domain.deeplinks.overview

import com.infinum.sentinel.domain.deeplinks.models.Variant
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus

/**
 * A variant with its App Links verification status.
 *
 * @property variant the saved variant.
 * @property status the status of the variant's host. Null for a custom scheme, a missing host, or when there is no snapshot.
 */
internal data class VariantEntry(
    val variant: Variant,
    val status: VerificationStatus? = null,
)
