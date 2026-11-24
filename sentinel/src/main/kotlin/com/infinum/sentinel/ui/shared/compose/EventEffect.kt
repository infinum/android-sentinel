package com.infinum.sentinel.ui.shared.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Collects one-off events from [flow] while the screen is at least started and passes each non-null one to
 * [onEvent].
 *
 * This is the same window `BaseView.collectFlows` uses for the XML screens. The view models emit events
 * without replay, so an event that arrives while the screen is stopped is dropped, as it is in the XML screens.
 */
@Composable
internal fun <Event : Any> EventEffect(
    flow: Flow<Event?>,
    onEvent: (Event) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(flow, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { event -> event?.let(currentOnEvent) }
        }
    }
}
