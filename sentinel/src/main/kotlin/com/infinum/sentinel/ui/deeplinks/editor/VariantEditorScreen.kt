package com.infinum.sentinel.ui.deeplinks.editor

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.ui.deeplinks.dialogs.ConfirmationDialog
import com.infinum.sentinel.ui.shared.compose.EventEffect
import com.infinum.sentinel.ui.shared.compose.SentinelCapsText
import com.infinum.sentinel.ui.shared.compose.SentinelScaffold
import com.infinum.sentinel.ui.shared.compose.SentinelSnackbarVisuals
import com.infinum.sentinel.ui.shared.compose.SentinelTheme
import com.infinum.sentinel.ui.shared.compose.showReplacing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

/**
 * The variant editor: a new variant from a base link when [isNewVariant], or an existing variant. The title follows
 * [isNewVariant] from the first frame, so the pane title TalkBack announces doesn't change once the draft loads.
 *
 * Back and the close button ask before discarding unsaved changes; that dialog survives recreation. The field
 * named by [initialFocus] is focused once, on the first load. Content stays above the keyboard: the scaffold
 * pads it by the larger of the system bars and the IME, which needs `adjustResize` below Android 11.
 */
@Composable
internal fun VariantEditorScreen(
    state: VariantEditorState?,
    events: Flow<VariantEditorEvent?>,
    callbacks: VariantEditorCallbacks,
    initialFocus: VariantEditorFocus,
    isNewVariant: Boolean,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequesters = remember { VariantEditorFocusRequesters() }
    val scrollState = rememberScrollState()
    var isDiscardShown by rememberSaveable { mutableStateOf(false) }
    var pendingFocusRowId by rememberSaveable { mutableStateOf<Long?>(null) }
    var focusedFieldId by rememberSaveable { mutableStateOf<String?>(null) }
    val restoredFocusFieldId = remember { focusedFieldId }
    val focus = remember(focusRequesters) { EditorFieldFocus(focusRequesters) { focusedFieldId = it } }
    // While Saved closes the screen, keep showing the last form, so the title and fields don't flash.
    val shownState = rememberLastShownState(state)
    val data = shownState as? VariantEditorState.Data
    val isDirty = state is VariantEditorState.Data && state.isDirty
    val requestClose = { if (isDirty) isDiscardShown = true else callbacks.onClose() }

    BackHandler(enabled = isDirty) { isDiscardShown = true }
    VariantEditorEventEffect(events, data, snackbarHostState, focusRequesters) { rowId -> pendingFocusRowId = rowId }
    // Keyed, so it runs once when Saved arrives and not again while the screen fades out.
    if (state == VariantEditorState.Saved) SideEffect(Unit) { callbacks.onClose() }

    val titleRes = if (isNewVariant) R.string.sentinel_deep_links_editor_new_title else R.string.sentinel_deep_links_editor_edit_title
    SentinelScaffold(
        title = stringResource(titleRes),
        navigationIcon = painterResource(R.drawable.sentinel_ic_close),
        navigationContentDescription = stringResource(R.string.sentinel_deep_links_editor_close),
        onNavigationClick = requestClose,
        isContentScrolled = scrollState.canScrollBackward,
        actions = { if (shownState !is VariantEditorState.Missing) SaveButton(onClick = callbacks.onSave) },
        snackbarHostState = snackbarHostState,
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).union(WindowInsets.ime),
    ) { padding ->
        VariantEditorBody(shownState, callbacks, focus, scrollState, Modifier.padding(padding))
    }
    if (data != null) {
        EditorFocusEffects(data, initialFocus, restoredFocusFieldId, pendingFocusRowId, focusRequesters) { pendingFocusRowId = null }
    }
    if (isDiscardShown) {
        DiscardChangesDialog(
            onDiscard = {
                isDiscardShown = false
                callbacks.onClose()
            },
            onKeepEditing = { isDiscardShown = false },
        )
    }
}

/**
 * Handles the view model's events: a row to focus, a refused save (Snackbar and focus on the first field to fix),
 * and a failed save (Snackbar).
 */
@Composable
private fun VariantEditorEventEffect(
    events: Flow<VariantEditorEvent?>,
    data: VariantEditorState.Data?,
    snackbarHostState: SnackbarHostState,
    focusRequesters: VariantEditorFocusRequesters,
    onFocusRow: (Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val fixErrorsText = stringResource(R.string.sentinel_deep_links_editor_fix_errors)
    val saveFailedText = stringResource(R.string.sentinel_deep_links_editor_save_failed)
    val showSnackbar = { message: String -> scope.launch { snackbarHostState.showReplacing(SentinelSnackbarVisuals(message)) } }
    EventEffect(flow = events) { event ->
        when (event) {
            is VariantEditorEvent.FocusRow -> onFocusRow(event.rowId)
            VariantEditorEvent.ValidationFailed -> {
                showSnackbar(fixErrorsText)
                data?.let(::firstInvalidFieldId)?.let(focusRequesters::find)?.let { requester ->
                    requester.requestFocus()
                    keyboard?.show()
                }
            }
            VariantEditorEvent.SaveFailed -> showSnackbar(saveFailedText)
        }
    }
}

/** The initial, restored and pending-row focus of the editor; see the effects for each rule. */
@Composable
private fun EditorFocusEffects(
    data: VariantEditorState.Data,
    initialFocus: VariantEditorFocus,
    restoredFocusFieldId: String?,
    pendingFocusRowId: Long?,
    focusRequesters: VariantEditorFocusRequesters,
    onPendingRowFocused: () -> Unit,
) {
    InitialFocusEffect(initialFocus = initialFocus, data = data, focusRequesters = focusRequesters)
    RestoreFocusEffect(focusedFieldId = restoredFocusFieldId, focusRequesters = focusRequesters)
    PendingRowFocusEffect(rowId = pendingFocusRowId, data = data, focusRequesters = focusRequesters, onFocused = onPendingRowFocused)
}

/** The form, or the missing message when the base link or variant is gone. */
@Composable
private fun VariantEditorBody(
    state: VariantEditorState?,
    callbacks: VariantEditorCallbacks,
    focus: EditorFieldFocus,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is VariantEditorState.Data ->
            VariantEditorFormContent(
                state = state,
                callbacks = callbacks,
                focus = focus,
                modifier = modifier,
                scrollState = scrollState,
            )
        VariantEditorState.Missing ->
            Text(
                text = stringResource(R.string.sentinel_deep_links_editor_missing),
                modifier = modifier.padding(all = 16.dp).testTag("missingView"),
                style = MaterialTheme.typography.bodyMedium,
            )
        VariantEditorState.Saved, null -> Unit
    }
}

/** Asks before closing the editor with unsaved changes. */
@Composable
private fun DiscardChangesDialog(
    onDiscard: () -> Unit,
    onKeepEditing: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(R.string.sentinel_deep_links_editor_discard_title),
        message = stringResource(R.string.sentinel_deep_links_editor_discard_message),
        confirmLabel = stringResource(R.string.sentinel_deep_links_editor_discard),
        dismissLabel = stringResource(R.string.sentinel_deep_links_editor_keep_editing),
        onConfirm = onDiscard,
        onDismiss = onKeepEditing,
    )
}

/**
 * [state], or while it is Saved the state shown before it. The remembered value is a cache that is never read for
 * invalidation, so updating it during composition is safe.
 */
@Composable
private fun rememberLastShownState(state: VariantEditorState?): VariantEditorState? {
    val lastShown = remember { arrayOfNulls<VariantEditorState>(1) }
    if (state != VariantEditorState.Saved) lastShown[0] = state
    return lastShown[0]
}

/** The toolbar's Save action, drawn in capitals like an XML action menu item. */
@Composable
private fun SaveButton(onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.testTag("save")) {
        SentinelCapsText(
            text = stringResource(R.string.sentinel_deep_links_editor_save),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private val previewCallbacks =
    VariantEditorCallbacks(
        onNameChanged = {},
        onHostChanged = {},
        onPathChanged = {},
        onKeyChanged = { _, _ -> },
        onValueChanged = { _, _ -> },
        onAddRow = {},
        onRemoveRow = {},
        onSave = {},
        onClose = {},
    )

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VariantEditorScreenPreview() {
    val spec = LinkSpec("myapp", HostSpec.Literal("catalog"), null, PathSpec.Prefix("/product/"))
    val form =
        VariantEditorForm(
            scheme = "myapp",
            hasAuthority = true,
            port = null,
            fragment = null,
            spec = spec,
            hostMode = FieldMode.READ_ONLY,
            pathMode = FieldMode.REQUIRED,
            fixedPath = null,
            baseText = "myapp://catalog/product/*",
        )
    val rows = listOf(VariantDraftRow(rowId = 0, key = "ref", value = "push"), VariantDraftRow(rowId = 1, key = "", value = "x"))
    val draft = VariantDraft(null, "base", null, "", "catalog", "/product/123", rows, nextRowId = 2)
    val validation =
        VariantDraftValidation(emptyKeyRowIds = setOf(1), isHostMissing = false, isPathMissing = false, isPatternMismatch = true)
    val link = "myapp://catalog/product/123?ref=push"
    val state = VariantEditorState.Data(form, draft, validation, link, isDirty = true, isShowingErrors = true)
    SentinelTheme {
        VariantEditorScreen(
            state = state,
            events = emptyFlow(),
            callbacks = previewCallbacks,
            initialFocus = VariantEditorFocus.NONE,
            isNewVariant = true,
        )
    }
}
