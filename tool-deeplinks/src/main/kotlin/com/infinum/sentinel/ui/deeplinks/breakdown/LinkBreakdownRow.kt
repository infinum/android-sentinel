package com.infinum.sentinel.ui.deeplinks.breakdown

import android.content.res.Configuration
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.ui.deeplinks.compose.SentinelCapsText
import com.infinum.sentinel.ui.deeplinks.compose.SentinelTheme
import com.infinum.sentinel.R as SentinelR

/**
 * A label and value row, the Compose counterpart of `sentinel_view_item_text`.
 *
 * The label is drawn in capitals unless [isLabelVerbatim] is true, which a query key needs because its case
 * matters. A long press calls [onCopy] with [label] and the real [value], so an empty value is copied as an
 * empty string and not as the placeholder shown on screen.
 */
@Composable
internal fun LinkBreakdownRow(
    label: String,
    value: String,
    isLabelVerbatim: Boolean,
    onCopy: (label: String, value: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowModifier =
        modifier
            .fillMaxWidth()
            .heightIn(min = dimensionResource(SentinelR.dimen.sentinel_item_height))
            .combinedClickable(
                onLongClickLabel = stringResource(R.string.sentinel_deep_links_action_copy_value),
                onLongClick = { onCopy(label, value) },
                onClick = {},
            )
            .padding(horizontal = 16.dp)
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowLabel(label = label, isVerbatim = isLabelVerbatim, modifier = Modifier.weight(weight = 0.5f))
        Text(
            text = value.ifEmpty { stringResource(R.string.sentinel_deep_links_breakdown_empty) },
            modifier = Modifier.weight(1f).padding(start = 8.dp, top = 8.dp, bottom = 8.dp).testTag("valueView"),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
        )
    }
}

@Composable
private fun RowLabel(
    label: String,
    isVerbatim: Boolean,
    modifier: Modifier = Modifier,
) {
    val labelModifier = modifier.padding(vertical = 8.dp).testTag("labelView")
    val style = MaterialTheme.typography.bodySmall
    // TextAppearance.Material3.BodySmall draws in the secondary text color.
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    if (isVerbatim) {
        Text(text = label, modifier = labelModifier, color = color, style = style)
    } else {
        SentinelCapsText(text = label, modifier = labelModifier, style = style, color = color)
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LinkBreakdownRowPreview() {
    SentinelTheme {
        LinkBreakdownRow(label = "Scheme", value = "https", isLabelVerbatim = false, onCopy = { _, _ -> })
    }
}
