package com.infinum.sentinel.ui.deeplinks.dialogs

import android.content.res.Configuration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infinum.sentinel.ui.shared.compose.SentinelTheme

/**
 * A two-button confirmation, such as Delete with Cancel or Discard with Keep editing.
 *
 * The message scrolls when it doesn't fit, as the message of a Material Components dialog does.
 * The caller decides when it shows and keeps that in saved state, so the dialog survives recreation.
 * [onConfirm] and [onDismiss] only report the choice; the caller hides the dialog.
 */
@Composable
internal fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(text = confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = dismissLabel) }
        },
        title = { Text(text = title) },
        text = { Text(text = message, modifier = Modifier.verticalScroll(rememberScrollState())) },
    )
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConfirmationDialogPreview() {
    SentinelTheme {
        ConfirmationDialog(
            title = "Delete this variant?",
            message = "myapp://shop/orders/42?promo=SUMMER",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
