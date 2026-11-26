package com.infinum.sentinel.ui.deeplinks

import android.content.res.Resources
import androidx.compose.material3.SnackbarHostState
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.LaunchMode
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksNotice
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.ui.deeplinks.compose.SentinelSnackbarVisuals
import com.infinum.sentinel.ui.deeplinks.compose.showReplacing
import com.infinum.sentinel.ui.deeplinks.editor.VariantEditorTarget
import com.infinum.sentinel.ui.deeplinks.launch.LaunchChoice
import com.infinum.sentinel.ui.deeplinks.launch.LaunchResult
import com.infinum.sentinel.ui.deeplinks.list.DeepLinksItem
import com.infinum.sentinel.ui.deeplinks.list.LinkRowAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The UI logic of the Deep links screen: what a row action, a launch or a notice action does, and which
 * Snackbar reports the outcome. The screen remembers one per composition; [actions] do the actual work.
 */
internal class DeepLinksScreenHandler(
    private val actions: DeepLinksScreenActions,
    private val resources: Resources,
    private val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope,
    private val showDialog: (DeepLinksDialog) -> Unit,
) {
    private companion object {
        /** A launch failure reason can be long, so its Snackbar gets more than the default two lines. */
        const val LAUNCH_FAILED_MAX_LINES = 5
    }

    fun onEvent(event: DeepLinksEvent) {
        when (event) {
            DeepLinksEvent.ChangeFailed -> showSnackbar(resources.getString(R.string.sentinel_deep_links_change_failed))
        }
    }

    fun openBreakdown(row: DeepLinksItem.LinkRow) = actions.openBreakdown(row.link)

    fun addVariant(row: DeepLinksItem.BaseLinkRow) {
        val target =
            VariantEditorTarget.NewFromBase(
                baseKey = row.baseLink.key,
                componentName = (row.baseLink.origin as? BaseLinkOrigin.Declared)?.componentName,
            )
        actions.openEditor(target)
    }

    /** How launching [row] starts, see [LaunchChoice.forLink]. Null for a row without a launch link, a declared template. */
    fun launchChoice(row: DeepLinksItem.LinkRow): LaunchChoice? {
        val warnings =
            when (row) {
                is DeepLinksItem.BaseLinkRow -> row.baseLink.warnings
                is DeepLinksItem.VariantRow -> row.baseLink?.warnings.orEmpty()
            }
        return row.launchLink?.let { LaunchChoice.forLink(it, warnings) }
    }

    /** Launches [row] in [mode]. A row without a launch link, a declared template, does nothing. */
    fun launch(
        row: DeepLinksItem.LinkRow,
        mode: LaunchMode,
    ) {
        row.launchLink?.let { launch(it, mode, isRetry = false) }
    }

    /** Launches [link] in [mode], for a variant the editor saved with its Launch action. */
    fun launchSaved(
        link: String,
        mode: LaunchMode,
    ) = launch(link, mode, isRetry = false)

    fun onNoticeAction(notice: DeepLinksNotice) {
        if (notice == DeepLinksNotice.LINK_HANDLING_OFF) openLinkSettings()
    }

    /** Opens the system link settings and reports with a Snackbar when no settings screen opened. */
    fun openLinkSettings() {
        if (!actions.openLinkSettings()) showSnackbar(resources.getString(R.string.sentinel_deep_links_notice_link_settings_failed))
    }

    fun onRowAction(
        row: DeepLinksItem.LinkRow,
        action: LinkRowAction,
    ) {
        when (action) {
            LinkRowAction.BREAKDOWN -> actions.openBreakdown(row.link)
            LinkRowAction.COPY -> actions.copy(row.title ?: row.link, row.link)
            LinkRowAction.SHARE -> actions.share(row.link)
            else -> (row as? DeepLinksItem.VariantRow)?.let { onVariantAction(it, action) }
        }
    }

    fun delete(variantId: String) = actions.delete(variantId)

    fun deleteAll() = actions.deleteAll()

    private fun onVariantAction(
        row: DeepLinksItem.VariantRow,
        action: LinkRowAction,
    ) {
        val target = VariantEditorTarget.Existing(row.variant.id)
        when (action) {
            LinkRowAction.EDIT -> actions.openEditor(target)
            LinkRowAction.DUPLICATE -> actions.duplicate(row.variant.id, resources.getString(R.string.sentinel_deep_links_copy_suffix))
            LinkRowAction.DELETE -> showDialog(DeepLinksDialog.DeleteVariant(variantId = row.variant.id, link = row.displayLink))
            else -> Unit
        }
    }

    /** Launches [link] in [mode]. A [isRetry] launch comes from a Snackbar action and offers no further retry. */
    private fun launch(
        link: String,
        mode: LaunchMode,
        isRetry: Boolean,
    ) {
        when (val result = actions.launch(link, mode)) {
            LaunchResult.Launched -> Unit
            // A screen whose filter lacks BROWSABLE can't take a Real tap, but it can take the link directly.
            LaunchResult.NoHandler ->
                showSnackbar(
                    message = resources.getString(R.string.sentinel_deep_links_no_handler),
                    actionLabel = resources.getString(R.string.sentinel_deep_links_open_in_app).takeUnless { isRetry },
                    onAction = { launch(link, LaunchMode.OPEN_IN_APP, isRetry = true) },
                )

            LaunchResult.NotHandledByApp ->
                showSnackbar(
                    message = resources.getString(R.string.sentinel_deep_links_not_handled),
                    actionLabel = resources.getString(R.string.sentinel_deep_links_try_real_tap).takeUnless { isRetry },
                    onAction = { launch(link, LaunchMode.REAL_TAP, isRetry = true) },
                )

            is LaunchResult.Failed ->
                showSnackbar(
                    message = resources.getString(R.string.sentinel_deep_links_launch_failed, result.reason),
                    maxLines = LAUNCH_FAILED_MAX_LINES,
                )
        }
    }

    private fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        maxLines: Int = SentinelSnackbarVisuals.DEFAULT_MAX_LINES,
        onAction: () -> Unit = {},
    ) {
        scope.launch {
            val visuals = SentinelSnackbarVisuals(message = message, actionLabel = actionLabel, maxLines = maxLines)
            if (snackbarHostState.showReplacing(visuals)) onAction()
        }
    }
}
