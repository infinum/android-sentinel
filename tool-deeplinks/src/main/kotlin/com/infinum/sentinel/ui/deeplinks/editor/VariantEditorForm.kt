package com.infinum.sentinel.ui.deeplinks.editor

import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec

/**
 * The fixed parts of the editor: what the base link dictates and which fields the tester fills in.
 *
 * @property scheme the link scheme.
 * @property hasAuthority true when the link has the `//` part.
 * @property port the declared port, if any.
 * @property fragment the fragment kept as is, already percent-encoded.
 * @property spec the declared shape. Null when the base link is gone. A field that can be filled in takes its
 * hint from the matching part of the spec.
 * @property hostMode whether the host is fixed, required or optional.
 * @property pathMode whether the path is fixed, required or optional.
 * @property fixedPath the percent-encoded path copied from an example link or from a variant whose base link is
 * gone. When set, the link uses it unchanged, so encoded characters such as `%2F` survive. Null when the path is
 * built from the draft.
 * @property baseText the base link's display text, or the variant's link when the base is gone.
 * @property warnings the base link's configuration warnings, which decide how Launch starts. Empty when the base link
 * is an example or is gone.
 * @property userInfo the user info of an example link or a variant, kept as written; null when it has none.
 */
internal data class VariantEditorForm(
    val scheme: String,
    val hasAuthority: Boolean,
    val port: Int?,
    val fragment: String?,
    val spec: LinkSpec?,
    val hostMode: FieldMode,
    val pathMode: FieldMode,
    val fixedPath: String?,
    val baseText: String,
    val warnings: Set<ConfigurationWarning> = emptySet(),
    val userInfo: String? = null,
)
