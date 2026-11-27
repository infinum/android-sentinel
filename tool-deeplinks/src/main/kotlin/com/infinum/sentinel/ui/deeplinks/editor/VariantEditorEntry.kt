package com.infinum.sentinel.ui.deeplinks.editor

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infinum.sentinel.ui.deeplinks.DeepLinksKey
import com.infinum.sentinel.ui.deeplinks.di.DeepLinksGraph
import com.infinum.sentinel.ui.deeplinks.launch.LaunchRequest

private const val KEY_DRAFT = "KEY_VARIANT_EDITOR_DRAFT"

/**
 * The editor entry of the Deep links navigation: edits the variant named by [key] with its own view model.
 *
 * The draft is saved in the entry's saved state, so after process death the new view model brings back what was
 * typed.
 */
@Composable
internal fun VariantEditorEntry(
    key: DeepLinksKey.Editor,
    onClose: (LaunchRequest?) -> Unit,
) {
    val viewModel =
        viewModel {
            VariantEditorViewModel(DeepLinksGraph.deepLinks, DeepLinksGraph.deepLinkVariants).also { created ->
                val savedState = createSavedStateHandle()
                created.setTarget(key.target)
                savedState.get<Bundle>(KEY_DRAFT)?.getString(KEY_DRAFT)?.let(created::restore)
                savedState.setSavedStateProvider(KEY_DRAFT) { Bundle().apply { putString(KEY_DRAFT, created.savedStateJson()) } }
                created.data()
            }
        }
    val currentOnClose by rememberUpdatedState(onClose)
    // The view model's flow getters return a new wrapper on every read, so read them once.
    val states = remember(viewModel) { viewModel.stateFlow }
    val events = remember(viewModel) { viewModel.eventFlow }
    val callbacks =
        remember(viewModel) {
            VariantEditorCallbacks(
                onNameChanged = viewModel::onNameChanged,
                onHostChanged = viewModel::onHostChanged,
                onPathChanged = viewModel::onPathChanged,
                onKeyChanged = viewModel::onKeyChanged,
                onValueChanged = viewModel::onValueChanged,
                onAddRow = viewModel::addRow,
                onRemoveRow = viewModel::removeRow,
                onSave = { viewModel.save() },
                onSaveAndLaunch = { mode -> viewModel.save(mode) },
                onClose = { launch -> currentOnClose(launch) },
            )
        }
    VariantEditorScreen(
        state = states.collectAsStateWithLifecycle().value,
        events = events,
        callbacks = callbacks,
        isNewVariant = key.target is VariantEditorTarget.NewFromBase,
    )
}
