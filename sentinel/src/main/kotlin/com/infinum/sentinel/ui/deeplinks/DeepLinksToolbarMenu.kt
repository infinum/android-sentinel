package com.infinum.sentinel.ui.deeplinks

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.infinum.sentinel.R

/**
 * The toolbar overflow of the Deep links screen. "Open link settings" shows when [isLinkSettingsAvailable]
 * (Android 12 and later), and "Delete all variants" is enabled when [variantCount] is above zero.
 */
@Composable
internal fun DeepLinksToolbarMenu(
    isLinkSettingsAvailable: Boolean,
    variantCount: Int,
    onOpenLinkSettings: () -> Unit,
    onDeleteAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { isExpanded = true }) {
            Icon(
                painter = painterResource(R.drawable.sentinel_ic_more),
                contentDescription = stringResource(R.string.sentinel_deep_links_more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            if (isLinkSettingsAvailable) {
                DropdownMenuItem(
                    text = { MenuText(R.string.sentinel_deep_links_menu_open_link_settings) },
                    onClick = {
                        isExpanded = false
                        onOpenLinkSettings()
                    },
                )
            }
            DropdownMenuItem(
                text = { MenuText(R.string.sentinel_deep_links_menu_delete_all) },
                onClick = {
                    isExpanded = false
                    onDeleteAll()
                },
                enabled = variantCount > 0,
            )
        }
    }
}

/** A menu entry in bodyLarge, the text size of the XML popup menus. */
@Composable
private fun MenuText(@StringRes textRes: Int) {
    Text(text = stringResource(textRes), style = MaterialTheme.typography.bodyLarge)
}
