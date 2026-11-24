package com.infinum.sentinel.ui.deeplinks.editor

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDirection

/**
 * An outlined, single-line field of the variant editor, the counterpart of a `TextInputLayout.OutlinedBox`.
 *
 * The field keeps its own text, seeded once from [initialText] and saved across recreation, and reports each
 * change through [onTextChange]. The view model's draft is never written back into the field, so typing never
 * moves the cursor. [error] replaces [helper] while it is shown, as in a `TextInputLayout`. The keyboard's action
 * defaults to Next, which moves to the following field, and autocorrect is off, as in the XML form. The field
 * registers its focus requester under [fieldId] in [focus].
 */
@Composable
internal fun EditorTextField(
    fieldId: String,
    initialText: String,
    label: String,
    tag: String,
    focus: EditorFieldFocus,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    helper: String? = null,
    error: String? = null,
    isLeftToRight: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Next),
) {
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }
    val requester = remember { FocusRequester() }
    DisposableEffect(fieldId, focus.requesters) {
        focus.requesters.register(fieldId, requester)
        onDispose { focus.requesters.unregister(fieldId, requester) }
    }
    val supporting = error ?: helper
    val supportingTag = if (error != null) "textinput_error" else "textinput_helper_text"
    val textStyle = if (isLeftToRight) LocalTextStyle.current.copy(textDirection = TextDirection.Ltr) else LocalTextStyle.current
    val fieldModifier = modifier.focusRequester(requester).onFocusChanged { if (it.isFocused) focus.onFocused(fieldId) }
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val isTextChanged = newValue.text != value.text
            value = newValue
            if (isTextChanged) onTextChange(newValue.text)
        },
        modifier = fieldModifier.testTag(tag),
        enabled = enabled,
        textStyle = textStyle,
        label = { Text(text = label) },
        supportingText = supporting?.let { text -> { Text(text = text, modifier = Modifier.testTag(supportingTag)) } },
        isError = error != null,
        keyboardOptions = keyboardOptions,
        singleLine = true,
    )
}
