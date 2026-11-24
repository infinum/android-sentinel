package com.infinum.sentinel.ui.deeplinks.list

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * The overflow menu of a list row, with the entries of [LinkRowAction.forRow]. Place it in the same box as the row's
 * More options button so it anchors there. [onAction] is called after [onDismiss].
 */
@Composable
internal fun LinkRowMenu(
    row: DeepLinksItem.LinkRow,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onAction: (LinkRowAction) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        LinkRowAction.forRow(row).forEach { action ->
            DropdownMenuItem(
                text = { Text(text = stringResource(action.labelRes), style = MaterialTheme.typography.bodyLarge) },
                onClick = {
                    onDismiss()
                    onAction(action)
                },
            )
        }
    }
}
