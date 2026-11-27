package com.infinum.sentinel.ui.deeplinks.compose

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals

/**
 * Shows [visuals] after dismissing the Snackbar on screen, so the newest message replaces it, as a Material
 * Components Snackbar does, instead of waiting in the queue. Returns true when the user tapped the action.
 */
internal suspend fun SnackbarHostState.showReplacing(visuals: SnackbarVisuals): Boolean {
    currentSnackbarData?.dismiss()
    return showSnackbar(visuals) == SnackbarResult.ActionPerformed
}
