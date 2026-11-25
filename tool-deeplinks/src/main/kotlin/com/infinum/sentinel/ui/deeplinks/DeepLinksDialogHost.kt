package com.infinum.sentinel.ui.deeplinks

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.ui.deeplinks.dialogs.ConfirmationDialog
import com.infinum.sentinel.ui.deeplinks.verification.VerificationStatusDialog

/**
 * Shows [dialog]. Every button hides it through [onDismiss] before acting, so a dialog never stays on screen over
 * the screen it opens.
 */
@Composable
internal fun DeepLinksDialogHost(
    dialog: DeepLinksDialog,
    variantCount: Int,
    handler: DeepLinksScreenHandler,
    onDismiss: () -> Unit,
) {
    when (dialog) {
        is DeepLinksDialog.DeleteVariant ->
            ConfirmationDialog(
                title = stringResource(R.string.sentinel_deep_links_delete_variant_title),
                message = dialog.link,
                confirmLabel = stringResource(R.string.sentinel_deep_links_delete),
                dismissLabel = stringResource(R.string.sentinel_deep_links_cancel),
                onConfirm = {
                    onDismiss()
                    handler.delete(dialog.variantId)
                },
                onDismiss = onDismiss,
            )

        DeepLinksDialog.DeleteAll ->
            ConfirmationDialog(
                title = stringResource(R.string.sentinel_deep_links_delete_all_title),
                message = pluralStringResource(R.plurals.sentinel_deep_links_delete_all_message, variantCount, variantCount),
                confirmLabel = stringResource(R.string.sentinel_deep_links_delete),
                dismissLabel = stringResource(R.string.sentinel_deep_links_cancel),
                onConfirm = {
                    onDismiss()
                    handler.deleteAll()
                },
                onDismiss = onDismiss,
            )

        is DeepLinksDialog.Status ->
            VerificationStatusDialog(
                status = dialog.status,
                onOpenLinkSettings = {
                    onDismiss()
                    handler.openLinkSettings()
                },
                onDismiss = onDismiss,
            )
    }
}
