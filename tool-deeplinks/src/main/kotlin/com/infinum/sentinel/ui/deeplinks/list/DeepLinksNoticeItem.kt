package com.infinum.sentinel.ui.deeplinks.list

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksNotice
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.ui.deeplinks.compose.SentinelTheme

/**
 * Shows a notice above the sections. A new [DeepLinksNotice] entry doesn't compile until both mappings handle it.
 */
@Composable
internal fun DeepLinksNoticeItem(
    notice: DeepLinksNotice,
    onActionClick: (DeepLinksNotice) -> Unit,
    modifier: Modifier = Modifier,
) {
    val action = actionOf(notice)
    val noticeModifier =
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    Column(modifier = noticeModifier) {
        Text(
            text = stringResource(messageOf(notice)),
            modifier = Modifier.testTag("messageView"),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (action != null) {
            TextButton(
                onClick = { onActionClick(notice) },
                modifier = Modifier.align(Alignment.End).testTag("actionButton"),
            ) {
                Text(text = stringResource(action))
            }
        }
    }
}

@StringRes
private fun messageOf(notice: DeepLinksNotice): Int =
    when (notice) {
        DeepLinksNotice.LINK_HANDLING_OFF -> R.string.sentinel_deep_links_notice_link_handling_off
        DeepLinksNotice.DISCOVERY_FAILED -> R.string.sentinel_deep_links_discovery_failed
    }

@StringRes
private fun actionOf(notice: DeepLinksNotice): Int? =
    when (notice) {
        DeepLinksNotice.LINK_HANDLING_OFF -> R.string.sentinel_deep_links_menu_open_link_settings
        DeepLinksNotice.DISCOVERY_FAILED -> null
    }

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeepLinksNoticeItemLinkHandlingOffPreview() {
    SentinelTheme {
        DeepLinksNoticeItem(notice = DeepLinksNotice.LINK_HANDLING_OFF, onActionClick = {})
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeepLinksNoticeItemDiscoveryFailedPreview() {
    SentinelTheme {
        DeepLinksNoticeItem(notice = DeepLinksNotice.DISCOVERY_FAILED, onActionClick = {})
    }
}
