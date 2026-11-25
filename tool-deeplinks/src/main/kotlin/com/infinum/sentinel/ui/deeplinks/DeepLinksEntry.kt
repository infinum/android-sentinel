package com.infinum.sentinel.ui.deeplinks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infinum.sentinel.ui.deeplinks.di.DeepLinksGraph
import com.infinum.sentinel.ui.deeplinks.launch.LaunchRequest

/**
 * The list entry of [DeepLinksNavDisplay]: connects [DeepLinksScreen] to its view model, the activity's [host]
 * calls and navigation through [onOpen]. [pendingLaunch] is a launch the editor asked for; the list runs it and
 * calls [onPendingLaunchDone].
 */
@Composable
internal fun DeepLinksEntry(
    host: DeepLinksHostActions,
    onOpen: (DeepLinksKey) -> Unit,
    pendingLaunch: LaunchRequest?,
    onPendingLaunchDone: () -> Unit,
) {
    val viewModel = viewModel { DeepLinksViewModel(DeepLinksGraph.deepLinks, DeepLinksGraph.deepLinkVariants).also { it.data() } }
    val currentOnOpen by rememberUpdatedState(onOpen)
    // Coming back from system settings must update the statuses without reopening the tool. The entry's
    // lifecycle also resumes when the editor or the breakdown closes.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshVerification() }
    // The view model's flow getters return a new wrapper on every read, so read them once. A new instance on each
    // recomposition would restart the collectors and could drop an event emitted in between.
    val states = remember(viewModel) { viewModel.stateFlow }
    val events = remember(viewModel) { viewModel.eventFlow }
    val actions =
        remember(viewModel, host) {
            DeepLinksScreenActions(
                close = host.close,
                openBreakdown = { link -> currentOnOpen(DeepLinksKey.Breakdown(link)) },
                openEditor = { target -> currentOnOpen(DeepLinksKey.Editor(target)) },
                launch = host.launch,
                openLinkSettings = host.openLinkSettings,
                copy = host.copy,
                share = host.share,
                duplicate = viewModel::duplicate,
                delete = viewModel::delete,
                deleteAll = viewModel::deleteAll,
            )
        }
    DeepLinksScreen(
        state = states.collectAsStateWithLifecycle().value,
        events = events,
        actions = actions,
        pendingLaunch = pendingLaunch,
        onPendingLaunchDone = onPendingLaunchDone,
    )
}
