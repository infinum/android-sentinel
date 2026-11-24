package com.infinum.sentinel.ui.deeplinks

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.domain.deeplinks.models.Variant
import com.infinum.sentinel.domain.deeplinks.overview.BaseLinkEntry
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksNotice
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksOverview
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksSection
import com.infinum.sentinel.domain.deeplinks.overview.VariantEntry
import com.infinum.sentinel.ui.deeplinks.launch.LaunchChoice
import com.infinum.sentinel.ui.deeplinks.launch.LaunchModeMenu
import com.infinum.sentinel.ui.deeplinks.launch.LaunchRequest
import com.infinum.sentinel.ui.deeplinks.launch.LaunchResult
import com.infinum.sentinel.ui.deeplinks.list.DeepLinksEmptyState
import com.infinum.sentinel.ui.deeplinks.list.DeepLinksItem
import com.infinum.sentinel.ui.deeplinks.list.DeepLinksItemKeys
import com.infinum.sentinel.ui.deeplinks.list.DeepLinksList
import com.infinum.sentinel.ui.deeplinks.list.DeepLinksListCallbacks
import com.infinum.sentinel.ui.deeplinks.list.LinkRowMenu
import com.infinum.sentinel.ui.deeplinks.list.flatten
import com.infinum.sentinel.ui.shared.compose.EventEffect
import com.infinum.sentinel.ui.shared.compose.SentinelScaffold
import com.infinum.sentinel.ui.shared.compose.SentinelTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * The Deep links screen: notices, example links, the declared links grouped by screen, and saved variants.
 *
 * The open dialog is kept in saved state, so it survives rotation and process death. Menus close on rotation,
 * as the XML popups did. [actions] do everything that needs the activity. [pendingLaunch], from the editor's
 * Launch action, runs once and then [onPendingLaunchDone] is called.
 */
@Composable
internal fun DeepLinksScreen(
    state: DeepLinksState?,
    events: Flow<DeepLinksEvent?>,
    actions: DeepLinksScreenActions,
    pendingLaunch: LaunchRequest? = null,
    onPendingLaunchDone: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    var dialog by rememberSaveable(stateSaver = DeepLinksDialog.Saver) { mutableStateOf<DeepLinksDialog?>(null) }
    val handler = remember(actions, resources) { DeepLinksScreenHandler(actions, resources, snackbarHostState, scope) { dialog = it } }
    val listState = rememberLazyListState()
    val data = state as? DeepLinksState.Data
    EventEffect(flow = events, onEvent = handler::onEvent)
    PendingLaunchEffect(pendingLaunch = pendingLaunch, handler = handler, onDone = onPendingLaunchDone)
    SentinelScaffold(
        title = stringResource(R.string.sentinel_deep_links),
        navigationIcon = painterResource(R.drawable.sentinel_ic_close),
        navigationContentDescription = stringResource(R.string.sentinel_deep_links_close),
        onNavigationClick = actions.close,
        isContentScrolled = listState.canScrollBackward,
        actions = {
            DeepLinksToolbarMenu(
                isLinkSettingsAvailable = data?.isLinkSettingsAvailable == true,
                variantCount = data?.variantCount ?: 0,
                onOpenLinkSettings = handler::openLinkSettings,
                onDeleteAll = { dialog = DeepLinksDialog.DeleteAll },
            )
        },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        if (data != null) {
            DeepLinksContent(
                overview = data.overview,
                listState = listState,
                contentPadding = padding,
                handler = handler,
                onStatusClick = { row -> row.status?.let { dialog = DeepLinksDialog.Status(it) } },
            )
        }
    }
    // The delete-all message needs the variant count, so dialogs wait for the first state after a restore.
    val shownDialog = dialog
    if (shownDialog != null && data != null) {
        DeepLinksDialogHost(dialog = shownDialog, variantCount = data.variantCount, handler = handler, onDismiss = { dialog = null })
    }
}

/** Runs [pendingLaunch] once, with the list's Snackbars for the outcome, then calls [onDone]. */
@Composable
private fun PendingLaunchEffect(
    pendingLaunch: LaunchRequest?,
    handler: DeepLinksScreenHandler,
    onDone: () -> Unit,
) {
    val currentOnDone by rememberUpdatedState(onDone)
    LaunchedEffect(pendingLaunch) {
        if (pendingLaunch != null) {
            handler.launchSaved(pendingLaunch.link, pendingLaunch.mode)
            currentOnDone()
        }
    }
}

/** The list, or the empty state when discovery worked and there is nothing to show. */
@Composable
private fun DeepLinksContent(
    overview: DeepLinksOverview,
    listState: LazyListState,
    contentPadding: PaddingValues,
    handler: DeepLinksScreenHandler,
    onStatusClick: (DeepLinksItem.LinkRow) -> Unit,
) {
    if (overview.isEmpty) {
        DeepLinksEmptyState(modifier = Modifier.padding(contentPadding))
        return
    }
    val items = remember(overview) { overview.flatten() }
    // Menus follow the row's key, so a refresh that changes the row (its status, say) doesn't close them.
    var moreMenuKey by remember { mutableStateOf<String?>(null) }
    var launchMenuKey by remember { mutableStateOf<String?>(null) }
    val callbacks =
        DeepLinksListCallbacks(
            onRowClick = handler::openBreakdown,
            onLaunchClick = { row ->
                when (val choice = handler.launchChoice(row)) {
                    LaunchChoice.Ask -> launchMenuKey = DeepLinksItemKeys.rowKey(row)
                    is LaunchChoice.Direct -> handler.launch(row, choice.mode)
                    null -> Unit
                }
            },
            onMoreClick = { row -> moreMenuKey = DeepLinksItemKeys.rowKey(row) },
            onAddVariantClick = handler::addVariant,
            onStatusClick = onStatusClick,
            onNoticeActionClick = handler::onNoticeAction,
        )
    DeepLinksList(
        items = items,
        callbacks = callbacks,
        listState = listState,
        contentPadding = contentPadding,
        launchMenu = { row ->
            LaunchModeMenu(
                expanded = launchMenuKey == DeepLinksItemKeys.rowKey(row),
                onDismiss = { launchMenuKey = null },
                onPick = { mode -> handler.launch(row, mode) },
            )
        },
        moreMenu = { row ->
            LinkRowMenu(
                row = row,
                expanded = moreMenuKey == DeepLinksItemKeys.rowKey(row),
                onDismiss = { moreMenuKey = null },
                onAction = { action -> handler.onRowAction(row, action) },
            )
        },
    )
}

private val previewActions =
    DeepLinksScreenActions(
        close = {},
        openBreakdown = {},
        openEditor = {},
        launch = { _, _ -> LaunchResult.Launched },
        openLinkSettings = { true },
        copy = { _, _ -> },
        share = {},
        duplicate = { _, _ -> },
        delete = {},
        deleteAll = {},
    )

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeepLinksScreenEmptyPreview() {
    val overview = DeepLinksOverview(notices = listOf(), sections = listOf(), isEmpty = true)
    SentinelTheme {
        DeepLinksScreen(
            state = DeepLinksState.Data(overview = overview, variantCount = 0, isLinkSettingsAvailable = true),
            events = emptyFlow(),
            actions = previewActions,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeepLinksScreenPreview() {
    val spec = LinkSpec("myapp", HostSpec.Literal("shop"), null, PathSpec.Prefix("/orders/"))
    val baseLink = BaseLink(key = "orders", origin = BaseLinkOrigin.Declared(PREVIEW_COMPONENT), spec = spec, exampleUri = null)
    val variant =
        Variant(
            id = "v1",
            name = "Order 42",
            baseKey = "orders",
            componentName = PREVIEW_COMPONENT,
            uri = DeepLinkUri("myapp", true, "shop", null, "/orders/42", listOf(), null),
        )
    val section =
        DeepLinksSection.Screen(
            componentName = PREVIEW_COMPONENT,
            title = "Orders",
            entries = listOf(BaseLinkEntry(baseLink = baseLink, variants = listOf(VariantEntry(variant)))),
        )
    val overview = DeepLinksOverview(notices = listOf(DeepLinksNotice.LINK_HANDLING_OFF), sections = listOf(section), isEmpty = false)
    SentinelTheme {
        DeepLinksScreen(
            state = DeepLinksState.Data(overview = overview, variantCount = 1, isLinkSettingsAvailable = true),
            events = emptyFlow(),
            actions = previewActions,
        )
    }
}

private const val PREVIEW_COMPONENT = "com.example.OrdersActivity"
