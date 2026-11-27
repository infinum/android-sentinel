package com.infinum.sentinel.ui.deeplinks.list

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.ui.deeplinks.compose.SentinelTheme
import com.infinum.sentinel.R as SentinelR

/**
 * Shows a base link: example label, link text, chips, variant count and actions.
 *
 * Add variant sits at the end of the chip line, centred on the chips. The variant count, or a template's hint, gets
 * its own line below only when there is one, so a row without variants doesn't end in an empty band.
 *
 * [launchMenu] and [moreMenu] are drawn inside the box of their button, so a DropdownMenu placed there anchors to it.
 */
@Composable
internal fun BaseLinkRowItem(
    row: DeepLinksItem.BaseLinkRow,
    callbacks: DeepLinksListCallbacks,
    modifier: Modifier = Modifier,
    launchMenu: @Composable () -> Unit = {},
    moreMenu: @Composable () -> Unit = {},
) {
    val rowModifier =
        modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClickLabel = stringResource(R.string.sentinel_deep_links_action_show_breakdown)) { callbacks.onRowClick(row) }
            .padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 4.dp)
    Column(modifier = rowModifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                LinkTitleText(title = row.title)
                LinkText(text = row.baseLink.displayText)
            }
            LinkRowActions(
                row = row,
                callbacks = callbacks,
                isLaunchShown = row.launchLink != null,
                launchMenu = launchMenu,
                moreMenu = moreMenu,
            )
        }
        ChipsLine(row = row, callbacks = callbacks)
        val summary = variantSummary(row)
        if (summary != null) {
            Text(
                text = summary,
                modifier = Modifier.padding(end = 12.dp, bottom = 8.dp).testTag("variantCountView"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** The chips, wrapping in the width Add variant leaves, and the Add variant button centred on them. */
@Composable
private fun ChipsLine(
    row: DeepLinksItem.BaseLinkRow,
    callbacks: DeepLinksListCallbacks,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            if (hasChips(row.baseLink, row.status)) {
                LinkChips(baseLink = row.baseLink, status = row.status, onStatusClick = { callbacks.onStatusClick(row) })
            }
        }
        TextButton(
            onClick = { callbacks.onAddVariantClick(row) },
            modifier = Modifier.testTag("addVariantButton"),
            enabled = row.baseLink.spec != null,
        ) {
            Icon(painter = painterResource(SentinelR.drawable.sentinel_ic_plus), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.sentinel_deep_links_add_variant))
        }
    }
}

/**
 * The variant count, or for a template without variants a hint that a variant is how to launch it.
 * Nothing when there are no variants otherwise, since "0 variants" on every row is noise.
 */
@Composable
private fun variantSummary(row: DeepLinksItem.BaseLinkRow): String? =
    when {
        row.variantCount > 0 -> pluralStringResource(R.plurals.sentinel_deep_links_variant_count, row.variantCount, row.variantCount)
        row.baseLink.isTemplate -> stringResource(R.string.sentinel_deep_links_template_hint)
        else -> null
    }

/** The example label or variant name, up to two lines. Draws nothing when [title] is null. */
@Composable
internal fun LinkTitleText(
    title: String?,
    modifier: Modifier = Modifier,
) {
    if (title != null) {
        Text(
            text = title,
            modifier = modifier.fillMaxWidth().testTag("titleView"),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

/** The link text, left to right whatever the locale, up to four lines. */
@Composable
internal fun LinkText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth().testTag("linkView"),
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
    )
}

/**
 * The Launch button, when [isLaunchShown], and the More options button of a row. Each sits in a [Box] with its
 * menu slot, so a menu anchors to its button. The icons are 20dp and use the primary color, as the Material
 * Components icon button style draws them.
 */
@Composable
internal fun RowScope.LinkRowActions(
    row: DeepLinksItem.LinkRow,
    callbacks: DeepLinksListCallbacks,
    isLaunchShown: Boolean,
    launchMenu: @Composable () -> Unit,
    moreMenu: @Composable () -> Unit,
) {
    if (isLaunchShown) {
        Box {
            IconButton(onClick = { callbacks.onLaunchClick(row) }, modifier = Modifier.testTag("launchButton")) {
                Icon(
                    painter = painterResource(R.drawable.sentinel_ic_launch),
                    contentDescription = stringResource(R.string.sentinel_deep_links_launch),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            launchMenu()
        }
    }
    Box {
        IconButton(onClick = { callbacks.onMoreClick(row) }, modifier = Modifier.testTag("moreButton")) {
            Icon(
                painter = painterResource(R.drawable.sentinel_ic_more),
                contentDescription = stringResource(R.string.sentinel_deep_links_more_options),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        moreMenu()
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

private fun previewBaseLink(
    spec: LinkSpec,
    warnings: Set<ConfigurationWarning> = setOf(),
) = BaseLink(
    key = "preview",
    origin = BaseLinkOrigin.Declared("com.example.ShopActivity"),
    spec = spec,
    exampleUri = null,
    warnings = warnings,
)

private val previewWebRow =
    previewBaseLink(
        spec = LinkSpec("https", HostSpec.Literal("example.com"), null, PathSpec.Literal("/orders")),
        warnings = setOf(ConfigurationWarning.DuplicateDeclaration(listOf("Promo"))),
    ).let {
        DeepLinksItem.BaseLinkRow(
            baseLink = it,
            variantCount = 2,
            title = "Orders",
            link = "https://example.com/orders",
            launchLink = "https://example.com/orders",
            status = VerificationStatus.VERIFIED,
        )
    }

private val previewTemplateRow =
    previewBaseLink(spec = LinkSpec("https", HostSpec.Wildcard(".example.com"), null, PathSpec.Prefix("/orders"))).let {
        DeepLinksItem.BaseLinkRow(
            baseLink = it,
            variantCount = 0,
            title = null,
            link = it.displayText,
            launchLink = null,
            status = null,
        )
    }

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BaseLinkRowItemPreview() {
    SentinelTheme {
        BaseLinkRowItem(row = previewWebRow, callbacks = previewCallbacks)
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BaseLinkRowItemTemplatePreview() {
    SentinelTheme {
        BaseLinkRowItem(row = previewTemplateRow, callbacks = previewCallbacks)
    }
}
