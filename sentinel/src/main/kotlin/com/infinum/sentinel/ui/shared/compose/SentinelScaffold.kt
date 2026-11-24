package com.infinum.sentinel.ui.shared.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.R

/**
 * The frame of a Sentinel Compose screen: [SentinelTopAppBar], [SentinelSnackbarHost] and the content on the
 * background color.
 *
 * Test tags are exposed as resource ids, so UI Automator dumps show them the way they show view ids in the XML
 * screens. [contentWindowInsets] defaults to the system bars and the display cutout, the insets the XML
 * screens apply.
 *
 * [title] is also the pane title, so TalkBack announces the screen when navigation changes it, the way it announces
 * a new activity.
 */
@Composable
internal fun SentinelScaffold(
    title: String,
    navigationIcon: Painter,
    navigationContentDescription: String,
    onNavigationClick: () -> Unit,
    modifier: Modifier = Modifier,
    isContentScrolled: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    contentWindowInsets: WindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
    content: @Composable (PaddingValues) -> Unit,
) {
    val scaffoldModifier =
        modifier.semantics {
            testTagsAsResourceId = true
            paneTitle = title
        }
    Scaffold(
        modifier = scaffoldModifier,
        topBar = {
            SentinelTopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                navigationContentDescription = navigationContentDescription,
                onNavigationClick = onNavigationClick,
                isContentScrolled = isContentScrolled,
                actions = actions,
            )
        },
        snackbarHost = { SentinelSnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SentinelScaffoldPreview() {
    SentinelTheme {
        SentinelScaffold(
            title = "Deep links",
            navigationIcon = painterResource(R.drawable.sentinel_ic_close),
            navigationContentDescription = "Close",
            onNavigationClick = {},
        ) { padding ->
            SentinelCapsText(
                text = "Example links",
                modifier = Modifier.padding(padding).padding(all = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                isHeading = true,
            )
        }
    }
}
