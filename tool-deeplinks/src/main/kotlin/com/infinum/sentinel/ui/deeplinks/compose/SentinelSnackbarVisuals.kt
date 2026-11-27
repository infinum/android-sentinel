package com.infinum.sentinel.ui.deeplinks.compose

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

/**
 * Snackbar content for [SentinelSnackbarHost], with an optional line limit for long messages.
 *
 * [maxLines] matches `Snackbar.setTextMaxLines` in the XML screens. A plain [SnackbarVisuals] gets
 * [DEFAULT_MAX_LINES], the Material Components default.
 */
internal data class SentinelSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    val maxLines: Int = DEFAULT_MAX_LINES,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = false,
) : SnackbarVisuals {
    companion object {
        const val DEFAULT_MAX_LINES = 2
    }
}
