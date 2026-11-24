package com.infinum.sentinel.ui.deeplinks.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.domain.deeplinks.models.Variant
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksNotice
import com.infinum.sentinel.ui.shared.compose.SentinelTheme

/**
 * The Deep links list: notices, section headers, base links and variants.
 *
 * A divider is drawn above each group of rows, see [DeepLinksItemKeys.hasDividerAbove]. [launchMenu] and
 * [moreMenu] are drawn inside the box of their button on every row, so a DropdownMenu placed there anchors to it.
 */
@Composable
internal fun DeepLinksList(
    items: List<DeepLinksItem>,
    callbacks: DeepLinksListCallbacks,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    launchMenu: @Composable (DeepLinksItem.LinkRow) -> Unit = {},
    moreMenu: @Composable (DeepLinksItem.LinkRow) -> Unit = {},
) {
    val keys = remember(items) { DeepLinksItemKeys.lazyKeys(items) }
    LazyColumn(modifier = modifier, state = listState, contentPadding = contentPadding) {
        itemsIndexed(items = items, key = { index, _ -> keys[index] }, contentType = { _, item -> item::class }) { index, item ->
            // Rows fade in and out and slide into place, as the XML list's default item animator did.
            Column(modifier = Modifier.fillMaxWidth().animateItem()) {
                if (DeepLinksItemKeys.hasDividerAbove(items, index)) {
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                }
                DeepLinksListItem(item = item, callbacks = callbacks, launchMenu = launchMenu, moreMenu = moreMenu)
            }
        }
    }
}

@Composable
private fun DeepLinksListItem(
    item: DeepLinksItem,
    callbacks: DeepLinksListCallbacks,
    launchMenu: @Composable (DeepLinksItem.LinkRow) -> Unit,
    moreMenu: @Composable (DeepLinksItem.LinkRow) -> Unit,
) {
    when (item) {
        is DeepLinksItem.Notice ->
            DeepLinksNoticeItem(notice = item.notice, onActionClick = callbacks.onNoticeActionClick)

        is DeepLinksItem.SectionHeader -> DeepLinksHeaderItem(item = item)
        is DeepLinksItem.ScreenHeader -> DeepLinksHeaderItem(item = item)
        is DeepLinksItem.BaseLinkRow ->
            BaseLinkRowItem(
                row = item,
                callbacks = callbacks,
                launchMenu = { launchMenu(item) },
                moreMenu = { moreMenu(item) },
            )

        is DeepLinksItem.VariantRow ->
            VariantRowItem(
                row = item,
                callbacks = callbacks,
                launchMenu = { launchMenu(item) },
                moreMenu = { moreMenu(item) },
            )
    }
}

private const val PREVIEW_COMPONENT = "com.example.ShopActivity"

private val previewCallbacks =
    DeepLinksListCallbacks(
        onRowClick = {},
        onLaunchClick = {},
        onMoreClick = {},
        onAddVariantClick = {},
        onStatusClick = {},
        onNoticeActionClick = {},
    )

private fun previewItems(): List<DeepLinksItem> {
    val baseLink =
        BaseLink(
            key = "orders",
            origin = BaseLinkOrigin.Declared(PREVIEW_COMPONENT),
            spec = LinkSpec("https", HostSpec.Literal("example.com"), null, PathSpec.Literal("/orders")),
            exampleUri = null,
        )
    val variant =
        Variant(
            id = "v1",
            name = "Summer promo",
            baseKey = "orders",
            componentName = PREVIEW_COMPONENT,
            uri = DeepLinkUri("https", true, "example.com", null, "/orders", listOf(), null),
        )
    return listOf(
        DeepLinksItem.Notice(DeepLinksNotice.LINK_HANDLING_OFF),
        DeepLinksItem.SectionHeader(R.string.sentinel_deep_links_examples),
        DeepLinksItem.ScreenHeader(componentName = PREVIEW_COMPONENT, title = "Deep link"),
        DeepLinksItem.BaseLinkRow(
            baseLink = baseLink,
            variantCount = 1,
            title = "Orders",
            link = "https://example.com/orders",
            launchLink = "https://example.com/orders",
            status = VerificationStatus.VERIFIED,
        ),
        DeepLinksItem.VariantRow(
            variant = variant,
            baseLink = baseLink,
            isOrphan = false,
            title = "Summer promo",
            link = "https://example.com/orders?promo=SUMMER",
            status = VerificationStatus.NOT_VERIFIED,
        ),
    )
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeepLinksListPreview() {
    SentinelTheme {
        DeepLinksList(items = previewItems(), callbacks = previewCallbacks)
    }
}
