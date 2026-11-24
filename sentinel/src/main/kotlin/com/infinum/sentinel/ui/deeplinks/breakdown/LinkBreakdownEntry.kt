package com.infinum.sentinel.ui.deeplinks.breakdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infinum.sentinel.ui.shared.compose.injectedViewModel

/**
 * The breakdown entry of the Deep links navigation: shows the breakdown of [link] with its own view model.
 */
@Composable
internal fun LinkBreakdownEntry(
    link: String,
    onBack: () -> Unit,
    onCopy: (label: String, value: String) -> Unit,
) {
    val viewModel =
        injectedViewModel<LinkBreakdownViewModel> { created ->
            created.setLink(link)
            created.data()
        }
    // The view model's flow getter returns a new wrapper on every read, so read it once.
    val states = remember(viewModel) { viewModel.stateFlow }
    LinkBreakdownScreen(state = states.collectAsStateWithLifecycle().value, onBackClick = onBack, onCopy = onCopy)
}
