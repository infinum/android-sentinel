package com.infinum.sentinel.ui.deeplinks.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.LaunchMode
import com.infinum.sentinel.ui.deeplinks.launch.LaunchChoice
import com.infinum.sentinel.ui.deeplinks.launch.LaunchModeMenu

/**
 * The editor toolbar's Launch action: saves the variant, closes the editor and launches the link, so a template can
 * be filled in and launched in one go. The launch mode follows the list's rule ([LaunchChoice.forLink]), so a web
 * link shows the Real tap / Open in this app menu first. A web draft that can't be saved goes to [onSave], which
 * shows its errors, instead of asking for a mode first; the view model validates every save either way.
 */
@Composable
internal fun SaveAndLaunchButton(
    data: VariantEditorState.Data,
    onSave: () -> Unit,
    onSaveAndLaunch: (LaunchMode) -> Unit,
) {
    var isMenuShown by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = {
                val choice = LaunchChoice.forLink(data.previewLink, data.form.warnings)
                when {
                    choice is LaunchChoice.Direct -> onSaveAndLaunch(choice.mode)
                    !data.validation.canSave -> onSave()
                    else -> isMenuShown = true
                }
            },
            modifier = Modifier.testTag("saveAndLaunchButton"),
        ) {
            Icon(
                painter = painterResource(R.drawable.sentinel_ic_launch),
                contentDescription = stringResource(R.string.sentinel_deep_links_editor_save_and_launch),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LaunchModeMenu(expanded = isMenuShown, onDismiss = { isMenuShown = false }, onPick = onSaveAndLaunch)
    }
}
