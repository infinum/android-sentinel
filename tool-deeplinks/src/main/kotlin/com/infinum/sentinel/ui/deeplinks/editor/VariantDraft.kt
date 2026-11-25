package com.infinum.sentinel.ui.deeplinks.editor

import kotlinx.serialization.Serializable

/**
 * What the tester has typed into the variant editor. Text is kept exactly as typed, plain and decoded.
 *
 * @property variantId id of the variant being edited, null for a new variant.
 * @property baseKey key of the base link the variant comes from.
 * @property componentName the declaring screen, if the base link is declared.
 * @property name the variant name as typed; blank means unnamed.
 * @property host the host as typed.
 * @property path the path as typed, plain text.
 * @property rows the query parameters in order.
 * @property nextRowId the id the next added row gets.
 */
@Serializable
internal data class VariantDraft(
    val variantId: String?,
    val baseKey: String,
    val componentName: String?,
    val name: String,
    val host: String,
    val path: String,
    val rows: List<VariantDraftRow>,
    val nextRowId: Long,
)
