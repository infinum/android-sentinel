package com.infinum.sentinel.ui.deeplinks.verification

import androidx.annotation.StringRes
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import com.infinum.sentinel.tool.deeplinks.R

/**
 * The texts of a [VerificationStatus], shared by the list chips and the explanation dialog.
 */
internal object VerificationStatusTexts {
    /** The short text of [status], shown on the chip and as the dialog title. */
    @StringRes
    fun labelOf(status: VerificationStatus): Int =
        when (status) {
            VerificationStatus.VERIFIED -> R.string.sentinel_deep_links_status_verified
            VerificationStatus.ENABLED_IN_SETTINGS -> R.string.sentinel_deep_links_status_enabled
            VerificationStatus.VERIFIED_HANDLING_OFF,
            VerificationStatus.ENABLED_HANDLING_OFF,
            -> R.string.sentinel_deep_links_status_handling_off
            VerificationStatus.NOT_VERIFIED -> R.string.sentinel_deep_links_status_not_verified
            VerificationStatus.NOT_REGISTERED -> R.string.sentinel_deep_links_status_not_registered
        }

    /** The longer explanation of [status], shown as the dialog message. */
    @StringRes
    fun explanationOf(status: VerificationStatus): Int =
        when (status) {
            VerificationStatus.VERIFIED -> R.string.sentinel_deep_links_status_verified_explanation
            VerificationStatus.ENABLED_IN_SETTINGS -> R.string.sentinel_deep_links_status_enabled_explanation
            VerificationStatus.VERIFIED_HANDLING_OFF -> R.string.sentinel_deep_links_status_verified_handling_off_explanation
            VerificationStatus.ENABLED_HANDLING_OFF -> R.string.sentinel_deep_links_status_enabled_handling_off_explanation
            VerificationStatus.NOT_VERIFIED -> R.string.sentinel_deep_links_status_not_verified_explanation
            VerificationStatus.NOT_REGISTERED -> R.string.sentinel_deep_links_status_not_registered_explanation
        }
}
