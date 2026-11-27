package com.infinum.sentinel.ui.deeplinks.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * The top app bar of Sentinel's Compose screens, matching the XML `MaterialToolbar` in an `AppBarLayout` with
 * `liftOnScroll`.
 *
 * The bar sits on the background color and moves to `surfaceContainer` while [isContentScrolled] is true, which
 * is how the XML app bar lifts. `TopAppBar` animates between the two container colors itself.
 *
 * Opt-in: `TopAppBar` is stable, but its `scrollBehavior` parameter has the experimental `TopAppBarScrollBehavior`
 * type, so every call needs the opt-in. This bar never passes a scroll behavior, so it doesn't depend on the
 * experimental part. This is the only Compose experimental opt-in in Sentinel; don't add others.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SentinelTopAppBar(
    title: String,
    navigationIcon: Painter,
    navigationContentDescription: String,
    onNavigationClick: () -> Unit,
    isContentScrolled: Boolean,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = if (isContentScrolled) colors.surfaceContainer else colors.background
    val insets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    TopAppBar(
        title = {
            Text(
                text = title,
                // TopAppBar already pads the title by 4dp; 4dp more puts it 8dp after the navigation button.
                modifier = Modifier.padding(start = 4.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        modifier = modifier.testTag("toolbar"),
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(painter = navigationIcon, contentDescription = navigationContentDescription)
            }
        },
        actions = actions,
        windowInsets = insets,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor),
    )
}
