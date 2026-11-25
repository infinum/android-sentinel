package com.infinum.sentinel.ui.deeplinks.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.ui.deeplinks.compose.SentinelTheme
import com.infinum.sentinel.ui.deeplinks.verification.VerificationStatusTexts

/**
 * A clickable verification status. A good status is green, like a valid certificate. NOT_VERIFIED and
 * NOT_REGISTERED are yellow and carry an info icon: they are often expected, for example on debug builds, so red
 * stays for broken declarations. The text carries the meaning, so color is never the only signal.
 */
@Composable
internal fun VerificationStatusChip(
    status: VerificationStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWarning = status == VerificationStatus.NOT_VERIFIED || status == VerificationStatus.NOT_REGISTERED
    val label = stringResource(VerificationStatusTexts.labelOf(status))
    val description = stringResource(R.string.sentinel_deep_links_status_content_description, label)
    val icon = if (isWarning) R.drawable.sentinel_ic_status_info else R.drawable.sentinel_ic_status_ok
    AssistChip(
        onClick = onClick,
        label = { Text(text = label, modifier = Modifier.testTag(if (isWarning) "statusWarningChip" else "statusChip")) },
        modifier = modifier.semantics { contentDescription = description },
        leadingIcon = { Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp)) },
        colors = statusChipColors(isWarning = isWarning),
        border = null,
    )
}

@Composable
private fun statusChipColors(isWarning: Boolean): ChipColors =
    if (isWarning) {
        AssistChipDefaults.assistChipColors(
            containerColor = colorResource(R.color.sentinel_warning_container),
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.onSurface,
        )
    } else {
        AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VerificationStatusChipPreview() {
    SentinelTheme {
        Row {
            VerificationStatusChip(status = VerificationStatus.VERIFIED, onClick = {})
            VerificationStatusChip(status = VerificationStatus.NOT_VERIFIED, onClick = {})
        }
    }
}
