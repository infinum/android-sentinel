package com.infinum.sentinel.ui.deeplinks.breakdown

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import com.infinum.sentinel.ui.shared.compose.SentinelCapsText
import com.infinum.sentinel.ui.shared.compose.SentinelScaffold
import com.infinum.sentinel.ui.shared.compose.SentinelTheme

/**
 * The Link breakdown screen: the full link, then its parts, query parameters and fragment.
 *
 * A null [state] shows the frame with an empty body. A long press on a row calls [onCopy] with its label and
 * value.
 */
@Composable
internal fun LinkBreakdownScreen(
    state: LinkBreakdownState?,
    onBackClick: () -> Unit,
    onCopy: (label: String, value: String) -> Unit,
) {
    val scrollState = rememberScrollState()
    SentinelScaffold(
        title = stringResource(R.string.sentinel_deep_links_breakdown_title),
        navigationIcon = painterResource(R.drawable.sentinel_ic_back),
        navigationContentDescription = stringResource(R.string.sentinel_deep_links_back),
        onNavigationClick = onBackClick,
        isContentScrolled = scrollState.canScrollBackward,
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(scaffoldPadding).padding(bottom = 32.dp),
        ) {
            when (state) {
                is LinkBreakdownState.Data -> BreakdownContent(breakdown = state.breakdown, onCopy = onCopy)
                null -> Unit
            }
        }
    }
}

@Composable
private fun BreakdownContent(
    breakdown: LinkBreakdown,
    onCopy: (label: String, value: String) -> Unit,
) {
    BreakdownHeader(text = stringResource(R.string.sentinel_deep_links_breakdown_full_link))
    SelectionContainer {
        Text(
            text = breakdown.fullLink,
            modifier = Modifier.padding(horizontal = 16.dp).testTag("fullLinkView"),
            style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
        )
    }
    when (breakdown) {
        is LinkBreakdown.Unparseable -> {
            Text(
                text = stringResource(R.string.sentinel_deep_links_breakdown_unparseable),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp).testTag("unparseableView"),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        is LinkBreakdown.Parsed -> ParsedContent(breakdown = breakdown, onCopy = onCopy)
    }
}

@Composable
private fun ParsedContent(
    breakdown: LinkBreakdown.Parsed,
    onCopy: (label: String, value: String) -> Unit,
) {
    PartRow(label = stringResource(R.string.sentinel_deep_links_breakdown_scheme), value = breakdown.scheme, onCopy = onCopy)
    breakdown.host?.let {
        PartRow(label = stringResource(R.string.sentinel_deep_links_breakdown_host), value = it, onCopy = onCopy)
    }
    breakdown.port?.let {
        PartRow(label = stringResource(R.string.sentinel_deep_links_breakdown_port), value = it.toString(), onCopy = onCopy)
    }
    PartRow(label = stringResource(R.string.sentinel_deep_links_breakdown_path), value = breakdown.path, onCopy = onCopy)
    if (breakdown.params.isNotEmpty()) {
        BreakdownHeader(
            text = stringResource(R.string.sentinel_deep_links_breakdown_parameters),
            modifier = Modifier.testTag("parametersHeaderView"),
        )
        breakdown.params.forEach { param ->
            LinkBreakdownRow(label = param.key, value = param.value, isLabelVerbatim = true, onCopy = onCopy)
        }
    }
    breakdown.fragment?.let {
        PartRow(label = stringResource(R.string.sentinel_deep_links_breakdown_fragment), value = it, onCopy = onCopy)
    }
}

@Composable
private fun PartRow(
    label: String,
    value: String,
    onCopy: (label: String, value: String) -> Unit,
) {
    LinkBreakdownRow(label = label, value = value, isLabelVerbatim = false, onCopy = onCopy)
}

@Composable
private fun BreakdownHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    val headerModifier = modifier.fillMaxWidth().height(48.dp).wrapContentHeight(Alignment.Bottom)
    SentinelCapsText(
        text = text,
        modifier = headerModifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        isHeading = true,
    )
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LinkBreakdownScreenParsedPreview() {
    val breakdown =
        LinkBreakdown.Parsed(
            fullLink = "https://example.com:8080/items/42?ref=home&empty=#details",
            scheme = "https",
            host = "example.com",
            port = 8080,
            path = "/items/42",
            params = listOf(QueryParam(key = "ref", value = "home"), QueryParam(key = "empty", value = "")),
            fragment = "details",
        )
    SentinelTheme {
        LinkBreakdownScreen(state = LinkBreakdownState.Data(breakdown), onBackClick = {}, onCopy = { _, _ -> })
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LinkBreakdownScreenUnparseablePreview() {
    SentinelTheme {
        LinkBreakdownScreen(
            state = LinkBreakdownState.Data(LinkBreakdown.Unparseable(fullLink = "not a link")),
            onBackClick = {},
            onCopy = { _, _ -> },
        )
    }
}
