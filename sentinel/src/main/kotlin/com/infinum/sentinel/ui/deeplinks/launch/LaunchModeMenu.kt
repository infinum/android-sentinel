package com.infinum.sentinel.ui.deeplinks.launch

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.LaunchMode

/**
 * Asks how to open a web link. Each mode shows its name and a short explanation, and a tap picks it at once.
 * Nothing is checked and nothing is remembered: the tester decides on every launch.
 *
 * Place it in the same box as the Launch button so it anchors there. [onPick] is called after [onDismiss].
 */
@Composable
internal fun LaunchModeMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onPick: (LaunchMode) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        LaunchMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = { LaunchModeText(mode = mode) },
                onClick = {
                    onDismiss()
                    onPick(mode)
                },
            )
        }
    }
}

@Composable
private fun LaunchModeText(mode: LaunchMode) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = stringResource(titleOf(mode)), style = MaterialTheme.typography.bodyLarge)
        Text(text = stringResource(descriptionOf(mode)), style = MaterialTheme.typography.bodyMedium)
    }
}

@StringRes
private fun titleOf(mode: LaunchMode): Int =
    when (mode) {
        LaunchMode.REAL_TAP -> R.string.sentinel_deep_links_real_tap
        LaunchMode.OPEN_IN_APP -> R.string.sentinel_deep_links_open_in_app
    }

@StringRes
private fun descriptionOf(mode: LaunchMode): Int =
    when (mode) {
        LaunchMode.REAL_TAP -> R.string.sentinel_deep_links_real_tap_description
        LaunchMode.OPEN_IN_APP -> R.string.sentinel_deep_links_open_in_app_description
    }
