package com.infinum.sentinel.ui.deeplinks.list

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkType
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import com.infinum.sentinel.ui.shared.compose.SentinelTheme

/**
 * The chips of a base link: type, verification status, template and the configuration warnings, in that order.
 * Draw it only when [hasChips] is true, since an empty row would still take space.
 */
@Composable
internal fun LinkChips(
    baseLink: BaseLink,
    status: VerificationStatus?,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val duplicate = baseLink.warnings.filterIsInstance<ConfigurationWarning.DuplicateDeclaration>().firstOrNull()
    FlowRow(
        modifier = modifier.fillMaxWidth().padding(end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (baseLink.spec != null) {
            SentinelTagChip(text = stringResource(typeLabel(baseLink.type)), tag = "typeChip")
        }
        if (status != null) {
            VerificationStatusChip(status = status, onClick = onStatusClick)
        }
        if (baseLink.isTemplate) {
            SentinelTagChip(text = stringResource(R.string.sentinel_deep_links_template), tag = "templateChip")
        }
        if (ConfigurationWarning.MissingDefault in baseLink.warnings) {
            SentinelTagChip(
                text = stringResource(R.string.sentinel_deep_links_warning_no_default),
                tag = "missingDefaultChip",
                isWarning = true,
            )
        }
        if (ConfigurationWarning.MissingBrowsable in baseLink.warnings) {
            SentinelTagChip(
                text = stringResource(R.string.sentinel_deep_links_warning_no_browsable),
                tag = "missingBrowsableChip",
                isWarning = true,
            )
        }
        if (duplicate != null) {
            val text = stringResource(R.string.sentinel_deep_links_warning_duplicate, duplicate.otherScreens.joinToString(", "))
            SentinelTagChip(text = text, tag = "duplicateChip", isWarning = true)
        }
    }
}

/** True when [LinkChips] would draw at least one chip for [baseLink] and [status]. */
internal fun hasChips(
    baseLink: BaseLink,
    status: VerificationStatus?,
): Boolean =
    baseLink.spec != null ||
        status != null ||
        baseLink.isTemplate ||
        baseLink.warnings.any {
            it == ConfigurationWarning.MissingDefault ||
                it == ConfigurationWarning.MissingBrowsable ||
                it is ConfigurationWarning.DuplicateDeclaration
        }

@StringRes
private fun typeLabel(type: LinkType): Int =
    when (type) {
        LinkType.CUSTOM_SCHEME -> R.string.sentinel_deep_links_custom_scheme
        LinkType.WEB -> R.string.sentinel_deep_links_web
    }

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LinkChipsPreview() {
    val warnings =
        setOf(
            ConfigurationWarning.MissingDefault,
            ConfigurationWarning.MissingBrowsable,
            ConfigurationWarning.DuplicateDeclaration(listOf("Promo")),
        )
    val baseLink =
        BaseLink(
            key = "shop",
            origin = BaseLinkOrigin.Declared("com.example.ShopActivity"),
            spec = LinkSpec("https", HostSpec.Wildcard("example.com"), null, PathSpec.Prefix("/orders")),
            exampleUri = null,
            warnings = warnings,
        )
    SentinelTheme {
        LinkChips(baseLink = baseLink, status = VerificationStatus.NOT_VERIFIED, onStatusClick = {})
    }
}
