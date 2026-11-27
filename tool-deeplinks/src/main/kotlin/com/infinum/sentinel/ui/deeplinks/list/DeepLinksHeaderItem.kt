package com.infinum.sentinel.ui.deeplinks.list

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.ui.deeplinks.compose.SentinelCapsText
import com.infinum.sentinel.ui.deeplinks.compose.SentinelTheme
import com.infinum.sentinel.R as SentinelR

/**
 * Shows the header of a section with a fixed title and, when it has one, where its links come from. The title is
 * marked as a heading so TalkBack can jump between sections.
 */
@Composable
internal fun DeepLinksHeaderItem(
    item: DeepLinksItem.SectionHeader,
    modifier: Modifier = Modifier,
) {
    val source = item.sourceRes?.let { stringResource(it) }
    DeepLinksHeaderItem(title = stringResource(item.titleRes), className = null, source = source, modifier = modifier)
}

/**
 * Shows the screen's title, and next to it the class name when the title is a label. A label alone, such as
 * "Deep link", reads like a category, while the class name makes clear that the section is a screen. The line below
 * says that the links come from the manifest, unlike the example links the app passes to the tool.
 */
@Composable
internal fun DeepLinksHeaderItem(
    item: DeepLinksItem.ScreenHeader,
    modifier: Modifier = Modifier,
) {
    val className = item.componentName.substringAfterLast('.').takeIf { it != item.title }
    val source = stringResource(R.string.sentinel_deep_links_source_manifest)
    DeepLinksHeaderItem(title = item.title, className = className, source = source, modifier = modifier)
}

/**
 * A header with a [title] in capitals and, when [className] is not null, the class name at the end. The title takes
 * the space the class name leaves. The class name takes at most half the width, and both texts end with an ellipsis
 * instead of being clipped. When [source] is not null, a second line says where the section's links come from.
 */
@Composable
internal fun DeepLinksHeaderItem(
    title: String,
    className: String?,
    source: String?,
    modifier: Modifier = Modifier,
) {
    val headerModifier =
        modifier
            .fillMaxWidth()
            .background(colorResource(SentinelR.color.sentinel_color_ripple))
            .padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
    Column(modifier = headerModifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SentinelCapsText(
                text = title,
                modifier = Modifier.weight(1f).padding(start = 16.dp, end = 8.dp).testTag("titleView"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                isHeading = true,
            )
            if (className != null) {
                Text(
                    text = className,
                    modifier = Modifier.atMostHalfWidth().padding(start = 8.dp, end = 16.dp).testTag("countView"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (source != null) {
            Text(
                text = source,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp).testTag("sourceView"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * Limits the element to half of the width it is offered. A Row measures the class name before the weighted title,
 * with the row's whole width, so this caps it at half the row. A layout modifier does this without the
 * subcomposition of a BoxWithConstraints in every list item.
 */
private fun Modifier.atMostHalfWidth(): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth / 2))
        layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
    }

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeepLinksHeaderItemSectionPreview() {
    SentinelTheme {
        DeepLinksHeaderItem(
            item = DeepLinksItem.SectionHeader(R.string.sentinel_deep_links_examples, R.string.sentinel_deep_links_source_examples),
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeepLinksHeaderItemScreenPreview() {
    SentinelTheme {
        DeepLinksHeaderItem(item = DeepLinksItem.ScreenHeader(componentName = "com.example.ShopActivity", title = "Deep link"))
    }
}
