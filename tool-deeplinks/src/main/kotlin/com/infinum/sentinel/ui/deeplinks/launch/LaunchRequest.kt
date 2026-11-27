package com.infinum.sentinel.ui.deeplinks.launch

import com.infinum.sentinel.domain.deeplinks.models.LaunchMode

/**
 * A launch the variant editor hands to the list after it saved a variant with its Launch action: [link] in [mode].
 * The list runs it, so its Snackbars report the outcome the same way as a launch from a row.
 */
internal data class LaunchRequest(
    val link: String,
    val mode: LaunchMode,
)
