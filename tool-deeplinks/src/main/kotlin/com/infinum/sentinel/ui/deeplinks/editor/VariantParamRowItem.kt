package com.infinum.sentinel.ui.deeplinks.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.R as SentinelR

/**
 * One query parameter row: the key and value fields and the remove button. Its fields keep their own text, so
 * the caller wraps each row in `key(rowId)`.
 */
@Composable
internal fun VariantParamRowItem(
    row: VariantDraftRow,
    isKeyErrorShown: Boolean,
    focus: EditorFieldFocus,
    callbacks: VariantEditorCallbacks,
    modifier: Modifier = Modifier,
) {
    // The XML fields used textNoSuggestions; autocorrect off is the closest stable option.
    val keyboard = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        EditorTextField(
            fieldId = VariantEditorFocusRequesters.key(row.rowId),
            initialText = row.key,
            label = stringResource(R.string.sentinel_deep_links_editor_key),
            tag = "keyInput",
            focus = focus,
            onTextChange = { callbacks.onKeyChanged(row.rowId, it) },
            modifier = Modifier.weight(1f),
            error = if (isKeyErrorShown) stringResource(R.string.sentinel_deep_links_editor_key_required) else null,
            isLeftToRight = true,
            keyboardOptions = keyboard,
        )
        EditorTextField(
            fieldId = VariantEditorFocusRequesters.value(row.rowId),
            initialText = row.value,
            label = stringResource(R.string.sentinel_deep_links_editor_value),
            tag = "valueInput",
            focus = focus,
            onTextChange = { callbacks.onValueChanged(row.rowId, it) },
            modifier = Modifier.weight(1f),
            isLeftToRight = true,
            keyboardOptions = keyboard,
        )
        IconButton(onClick = { callbacks.onRemoveRow(row.rowId) }, modifier = Modifier.padding(top = 8.dp).testTag("removeButton")) {
            Icon(
                painter = painterResource(SentinelR.drawable.sentinel_ic_close),
                contentDescription = stringResource(R.string.sentinel_deep_links_editor_remove_parameter),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
