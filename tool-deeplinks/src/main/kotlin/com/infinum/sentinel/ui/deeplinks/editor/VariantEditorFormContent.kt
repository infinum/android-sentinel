package com.infinum.sentinel.ui.deeplinks.editor

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.ui.deeplinks.compose.SentinelCapsText

/**
 * The form of the variant editor: the base link, the name, host and path fields, the query parameters, the final
 * link and the pattern mismatch warning. [scrollState] lets the screen lift its top bar.
 */
@Composable
internal fun VariantEditorFormContent(
    state: VariantEditorState.Data,
    callbacks: VariantEditorCallbacks,
    focus: EditorFieldFocus,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    val linkStyle = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr)
    Column(modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(bottom = 32.dp)) {
        SelectionContainer {
            Text(
                text = state.form.baseText,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp).testTag("baseLinkView"),
                style = linkStyle,
            )
        }
        LinkPartFields(state = state, callbacks = callbacks, focus = focus)
        EditorHeader(text = stringResource(R.string.sentinel_deep_links_editor_parameters), tag = "parametersHeaderView")
        ParamRows(state = state, callbacks = callbacks, focus = focus)
        TextButton(onClick = callbacks.onAddRow, modifier = Modifier.padding(start = 8.dp).testTag("addParameterButton")) {
            Text(text = stringResource(R.string.sentinel_deep_links_editor_add_parameter))
        }
        EditorHeader(text = stringResource(R.string.sentinel_deep_links_editor_final_link), tag = null)
        SelectionContainer {
            Text(
                text = state.previewLink,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("previewView"),
                style = linkStyle,
            )
        }
        if (state.validation.isPatternMismatch) {
            val warningModifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp).semantics { liveRegion = LiveRegionMode.Polite }
            Text(
                text = stringResource(R.string.sentinel_deep_links_editor_mismatch),
                modifier = warningModifier.testTag("warningView"),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** The name, host and path fields. A fixed host or path is shown disabled, and hidden when it is empty. */
@Composable
private fun LinkPartFields(
    state: VariantEditorState.Data,
    callbacks: VariantEditorCallbacks,
    focus: EditorFieldFocus,
) {
    val form = state.form
    val required = stringResource(R.string.sentinel_deep_links_editor_part_required)
    val fieldModifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 16.dp)
    EditorTextField(
        fieldId = VariantEditorFocusRequesters.NAME,
        initialText = state.draft.name,
        label = stringResource(R.string.sentinel_deep_links_editor_name),
        tag = "nameInput",
        focus = focus,
        onTextChange = callbacks.onNameChanged,
        modifier = fieldModifier,
    )
    if (form.hostMode != FieldMode.READ_ONLY || state.draft.host.isNotEmpty()) {
        EditorTextField(
            fieldId = VariantEditorFocusRequesters.HOST,
            initialText = state.draft.host,
            label = stringResource(R.string.sentinel_deep_links_editor_host),
            tag = "hostInput",
            focus = focus,
            onTextChange = callbacks.onHostChanged,
            modifier = fieldModifier,
            enabled = form.hostMode != FieldMode.READ_ONLY,
            helper = form.spec?.host?.takeIf { form.hostMode != FieldMode.READ_ONLY }?.let { hostHint(it) },
            error = required.takeIf { state.isShowingErrors && state.validation.isHostMissing },
            isLeftToRight = true,
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
        )
    }
    if (form.pathMode != FieldMode.READ_ONLY || state.draft.path.isNotEmpty()) {
        EditorTextField(
            fieldId = VariantEditorFocusRequesters.PATH,
            initialText = state.draft.path,
            label = stringResource(R.string.sentinel_deep_links_editor_path),
            tag = "pathInput",
            focus = focus,
            onTextChange = callbacks.onPathChanged,
            modifier = fieldModifier,
            enabled = form.pathMode != FieldMode.READ_ONLY,
            helper = form.spec?.path?.takeIf { form.pathMode != FieldMode.READ_ONLY }?.let { pathHint(it) },
            error = required.takeIf { state.isShowingErrors && state.validation.isPathMissing },
            isLeftToRight = true,
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
        )
    }
}

/** The parameter rows, each keyed by its row id so its fields keep their text when other rows come and go. */
@Composable
private fun ParamRows(
    state: VariantEditorState.Data,
    callbacks: VariantEditorCallbacks,
    focus: EditorFieldFocus,
) {
    state.draft.rows.forEach { row ->
        key(row.rowId) {
            VariantParamRowItem(
                row = row,
                isKeyErrorShown = state.isShowingErrors && row.rowId in state.validation.emptyKeyRowIds,
                focus = focus,
                callbacks = callbacks,
            )
        }
    }
}

/** A section header in capitals, like the XML headers with `textAllCaps`. */
@Composable
private fun EditorHeader(
    text: String,
    tag: String?,
) {
    val headerModifier = Modifier.fillMaxWidth().height(48.dp).wrapContentHeight(Alignment.Bottom)
    SentinelCapsText(
        text = text,
        modifier = headerModifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp).let { if (tag != null) it.testTag(tag) else it },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        isHeading = true,
    )
}
