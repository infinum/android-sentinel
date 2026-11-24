package com.infinum.sentinel.ui.deeplinks.verification

import android.content.res.Configuration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import com.infinum.sentinel.ui.shared.compose.SentinelTheme

/**
 * Explains a verification status: what it means for Real tap, plus the shared footer (FR-40).
 *
 * The text scrolls when it doesn't fit, for example in landscape or at a large font scale. A Not verified status
 * also offers Open link settings, which calls [onOpenLinkSettings]. The caller keeps the shown status in saved
 * state and hides the dialog in [onDismiss].
 */
@Composable
internal fun VerificationStatusDialog(
    status: VerificationStatus,
    onOpenLinkSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    val explanation = stringResource(VerificationStatusTexts.explanationOf(status))
    val footer = stringResource(R.string.sentinel_deep_links_status_footer)
    val closeButton: @Composable () -> Unit = {
        TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.sentinel_deep_links_status_close)) }
    }
    val isSettingsOffered = status == VerificationStatus.NOT_VERIFIED
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = if (isSettingsOffered) settingsButton(onOpenLinkSettings) else closeButton,
        dismissButton = if (isSettingsOffered) closeButton else null,
        title = { Text(text = stringResource(VerificationStatusTexts.labelOf(status))) },
        text = { Text(text = "$explanation\n\n$footer", modifier = Modifier.verticalScroll(rememberScrollState())) },
    )
}

private fun settingsButton(onClick: () -> Unit): @Composable () -> Unit =
    {
        TextButton(onClick = onClick) { Text(text = stringResource(R.string.sentinel_deep_links_menu_open_link_settings)) }
    }

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VerificationStatusDialogPreview() {
    SentinelTheme {
        VerificationStatusDialog(status = VerificationStatus.NOT_VERIFIED, onOpenLinkSettings = {}, onDismiss = {})
    }
}
