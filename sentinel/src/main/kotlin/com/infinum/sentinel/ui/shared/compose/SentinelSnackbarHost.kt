package com.infinum.sentinel.ui.shared.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Snackbar host styled like `Sentinel.Snackbar`: an `onBackground` container, `surface` text and a
 * `sentinel_primary` action.
 *
 * The message and action carry the `snackbar_text` and `snackbar_action` tags, the ids of a Material
 * Components Snackbar, so UI dumps find them the same way.
 */
@Composable
internal fun SentinelSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        SentinelSnackbar(data)
    }
}

@Composable
private fun SentinelSnackbar(data: SnackbarData) {
    val visuals = data.visuals
    val maxLines = (visuals as? SentinelSnackbarVisuals)?.maxLines ?: SentinelSnackbarVisuals.DEFAULT_MAX_LINES
    val actionLabel = visuals.actionLabel
    Snackbar(
        modifier = Modifier.padding(all = 12.dp),
        action = actionLabel?.let { label -> { SnackbarAction(label = label, onClick = data::performAction) } },
        containerColor = MaterialTheme.colorScheme.onBackground,
        contentColor = MaterialTheme.colorScheme.surface,
    ) {
        Text(
            text = visuals.message,
            modifier = Modifier.testTag("snackbar_text"),
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines,
        )
    }
}

@Composable
private fun SnackbarAction(
    label: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.testTag("snackbar_action"),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
    ) {
        Text(text = label)
    }
}
