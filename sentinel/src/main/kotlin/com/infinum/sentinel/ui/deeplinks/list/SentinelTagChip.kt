package com.infinum.sentinel.ui.deeplinks.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.ui.shared.compose.SentinelTheme

/**
 * A plain tag such as Web or Template. It is neither clickable nor focusable, so it doesn't read as a button.
 * A warning tag is red, like an expired certificate. The 8dp of vertical padding lines the tag up with the 48dp
 * touch target of the status chip.
 */
@Composable
internal fun SentinelTagChip(
    text: String,
    tag: String,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false,
) {
    val container = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
    val content = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
    Surface(
        modifier = modifier.padding(vertical = 8.dp),
        shape = RoundedCornerShape(size = 8.dp),
        color = container,
        contentColor = content,
    ) {
        Box(modifier = Modifier.heightIn(min = 32.dp), contentAlignment = Alignment.CenterStart) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp).testTag(tag),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SentinelTagChipPreview() {
    SentinelTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SentinelTagChip(text = "Web", tag = "typeChip")
            SentinelTagChip(text = "Links can't open this", tag = "missingDefaultChip", isWarning = true)
        }
    }
}
