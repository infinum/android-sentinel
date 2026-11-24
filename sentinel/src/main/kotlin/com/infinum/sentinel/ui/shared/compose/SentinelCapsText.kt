package com.infinum.sentinel.ui.shared.compose

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Text drawn in capitals, the Compose counterpart of `android:textAllCaps`.
 *
 * Only the drawing is uppercase. The semantics keep the original [text], so accessibility services and UI dumps
 * read "Scheme" rather than "SCHEME". This differs on purpose from an XML view with `textAllCaps`, whose UI dump
 * shows the uppercase text.
 */
@Composable
internal fun SentinelCapsText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    isHeading: Boolean = false,
) {
    val locale = LocalConfiguration.current.locales[0]
    val semanticsModifier =
        modifier.clearAndSetSemantics {
            this.text = AnnotatedString(text)
            if (isHeading) heading()
        }
    Text(
        text = text.uppercase(locale),
        modifier = semanticsModifier,
        color = color,
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        style = style,
    )
}
