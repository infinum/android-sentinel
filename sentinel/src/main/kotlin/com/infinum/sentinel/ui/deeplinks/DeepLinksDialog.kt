package com.infinum.sentinel.ui.deeplinks

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus

/**
 * The dialog the Deep links screen shows. [Saver] keeps it as plain strings, so an open dialog survives rotation
 * and process death, as a DialogFragment does.
 */
internal sealed interface DeepLinksDialog {
    /** Confirms deleting the variant [variantId], whose built link is [link]. */
    data class DeleteVariant(
        val variantId: String,
        val link: String,
    ) : DeepLinksDialog

    /** Confirms deleting every saved variant. */
    data object DeleteAll : DeepLinksDialog

    /** Explains [status]. */
    data class Status(
        val status: VerificationStatus,
    ) : DeepLinksDialog

    companion object {
        private const val DELETE_VARIANT = "deleteVariant"
        private const val DELETE_ALL = "deleteAll"
        private const val STATUS = "status"

        /** Saves the dialog as a type name followed by its values; no dialog is an empty list. */
        val Saver: Saver<DeepLinksDialog?, Any> =
            listSaver(
                save = { dialog -> dialog.toValues() },
                restore = { values -> values.toDialog() },
            )

        private fun DeepLinksDialog?.toValues(): List<String> =
            when (this) {
                null -> listOf()
                is DeleteVariant -> listOf(DELETE_VARIANT, variantId, link)
                DeleteAll -> listOf(DELETE_ALL)
                is Status -> listOf(STATUS, status.name)
            }

        private fun List<String>.toDialog(): DeepLinksDialog? =
            when (firstOrNull()) {
                DELETE_VARIANT -> DeleteVariant(variantId = this[1], link = this[2])
                DELETE_ALL -> DeleteAll
                STATUS -> VerificationStatus.entries.firstOrNull { it.name == this[1] }?.let(::Status)
                else -> null
            }
    }
}
