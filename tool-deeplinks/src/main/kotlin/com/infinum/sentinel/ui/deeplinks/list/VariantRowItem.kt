package com.infinum.sentinel.ui.deeplinks.list

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.Variant
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.ui.deeplinks.compose.SentinelTheme

/**
 * Shows a variant: its name, the readable form of its link, status chip, orphan chip and actions.
 *
 * [launchMenu] and [moreMenu] are drawn inside the box of their button, so a DropdownMenu placed there anchors to it.
 */
@Composable
internal fun VariantRowItem(
    row: DeepLinksItem.VariantRow,
    callbacks: DeepLinksListCallbacks,
    modifier: Modifier = Modifier,
    launchMenu: @Composable () -> Unit = {},
    moreMenu: @Composable () -> Unit = {},
) {
    val rowModifier =
        modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClickLabel = stringResource(R.string.sentinel_deep_links_action_show_breakdown)) { callbacks.onRowClick(row) }
            .padding(start = 32.dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
    Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
            LinkTitleText(title = row.title)
            LinkText(text = row.displayLink)
            if (row.status != null || row.isOrphan) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.status?.let { VerificationStatusChip(status = it, onClick = { callbacks.onStatusClick(row) }) }
                    if (row.isOrphan) {
                        SentinelTagChip(text = stringResource(R.string.sentinel_deep_links_no_longer_declared), tag = "orphanChip")
                    }
                }
            }
        }
        LinkRowActions(row = row, callbacks = callbacks, isLaunchShown = true, launchMenu = launchMenu, moreMenu = moreMenu)
    }
}

private val previewCallbacks =
    DeepLinksListCallbacks(
        onRowClick = {},
        onLaunchClick = {},
        onMoreClick = {},
        onAddVariantClick = {},
        onStatusClick = {},
        onNoticeActionClick = {},
    )

private val previewVariant =
    Variant(
        id = "preview",
        name = "Summer promo",
        baseKey = "preview",
        componentName = "com.example.ShopActivity",
        uri = DeepLinkUri("myapp", true, "shop", null, "/orders/42", listOf(), null),
    )

private fun previewVariantRow(
    status: VerificationStatus?,
    isOrphan: Boolean,
) = DeepLinksItem.VariantRow(
    variant = previewVariant,
    baseLink = null,
    isOrphan = isOrphan,
    title = "Summer promo",
    link = "myapp://shop/orders/42?promo=SUMMER",
    status = status,
    displayLink = "myapp://shop/orders/42?promo=SUMMER",
)

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VariantRowItemPreview() {
    SentinelTheme {
        VariantRowItem(row = previewVariantRow(status = VerificationStatus.NOT_VERIFIED, isOrphan = false), callbacks = previewCallbacks)
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VariantRowItemOrphanPreview() {
    SentinelTheme {
        VariantRowItem(row = previewVariantRow(status = null, isOrphan = true), callbacks = previewCallbacks)
    }
}
