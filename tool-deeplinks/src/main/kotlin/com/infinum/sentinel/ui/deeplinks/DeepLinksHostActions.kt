package com.infinum.sentinel.ui.deeplinks

import com.infinum.sentinel.domain.deeplinks.models.LaunchMode
import com.infinum.sentinel.ui.deeplinks.launch.LaunchResult

/**
 * What the Deep links screens need from the activity: the calls that need a `Context`.
 */
internal data class DeepLinksHostActions(
    /** Closes the tool. */
    val close: () -> Unit,
    /** Launches a link and reports the outcome, which the list turns into a Snackbar. */
    val launch: (link: String, mode: LaunchMode) -> LaunchResult,
    /** Opens the system link settings. False when no settings screen could be opened. */
    val openLinkSettings: () -> Boolean,
    val copy: (label: String, value: String) -> Unit,
    val share: (text: String) -> Unit,
)
