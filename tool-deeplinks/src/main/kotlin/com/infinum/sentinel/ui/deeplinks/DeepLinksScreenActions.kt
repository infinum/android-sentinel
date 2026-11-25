package com.infinum.sentinel.ui.deeplinks

import com.infinum.sentinel.domain.deeplinks.models.LaunchMode
import com.infinum.sentinel.ui.deeplinks.editor.VariantEditorTarget
import com.infinum.sentinel.ui.deeplinks.launch.LaunchResult

/**
 * What the Deep links screen asks its host to do. [DeepLinksEntry] builds it from the activity's calls that need a
 * `Context` (launching links, the clipboard and share sheet), navigation to the other screens and the view model's
 * variant changes, so the screen itself stays free of Android framework calls.
 */
internal data class DeepLinksScreenActions(
    val close: () -> Unit,
    val openBreakdown: (link: String) -> Unit,
    val openEditor: (target: VariantEditorTarget) -> Unit,
    /** Launches a link and reports the outcome, which the screen turns into a Snackbar. */
    val launch: (link: String, mode: LaunchMode) -> LaunchResult,
    /** Opens the system link settings. False when no settings screen could be opened. */
    val openLinkSettings: () -> Boolean,
    val copy: (label: String, value: String) -> Unit,
    val share: (text: String) -> Unit,
    val duplicate: (variantId: String, copySuffix: String) -> Unit,
    val delete: (variantId: String) -> Unit,
    val deleteAll: () -> Unit,
)
