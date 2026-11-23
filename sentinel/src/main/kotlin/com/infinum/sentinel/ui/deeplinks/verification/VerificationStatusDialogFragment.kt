package com.infinum.sentinel.ui.deeplinks.verification

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus

/**
 * Explains a verification status: what it means for Real tap, plus the shared footer (FR-40). A Not verified
 * status also offers Open link settings, which sends [RESULT_OPEN_LINK_SETTINGS] under [REQUEST_KEY]. Show it
 * on the host's child fragment manager and listen with
 * `childFragmentManager.setFragmentResultListener(REQUEST_KEY, viewLifecycleOwner)`.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class VerificationStatusDialogFragment : DialogFragment() {
    companion object {
        const val REQUEST_KEY: String = "REQUEST_VERIFICATION_STATUS"
        const val RESULT_OPEN_LINK_SETTINGS: String = "RESULT_OPEN_LINK_SETTINGS"
        const val TAG: String = "VerificationStatusDialogFragment"

        private const val KEY_STATUS = "KEY_VERIFICATION_STATUS"

        /** Creates the dialog for [status]. The status is kept in the arguments. */
        fun newInstance(status: VerificationStatus): VerificationStatusDialogFragment =
            VerificationStatusDialogFragment().apply {
                arguments = bundleOf(KEY_STATUS to status.name)
            }

        /** The short text of [status], shown on the chip and as the dialog title. */
        @StringRes
        fun labelOf(status: VerificationStatus): Int =
            when (status) {
                VerificationStatus.VERIFIED -> R.string.sentinel_deep_links_status_verified
                VerificationStatus.ENABLED_IN_SETTINGS -> R.string.sentinel_deep_links_status_enabled
                VerificationStatus.NOT_VERIFIED -> R.string.sentinel_deep_links_status_not_verified
                VerificationStatus.NOT_REGISTERED -> R.string.sentinel_deep_links_status_not_registered
            }

        @StringRes
        private fun explanationOf(status: VerificationStatus): Int =
            when (status) {
                VerificationStatus.VERIFIED -> R.string.sentinel_deep_links_status_verified_explanation
                VerificationStatus.ENABLED_IN_SETTINGS -> R.string.sentinel_deep_links_status_enabled_explanation
                VerificationStatus.NOT_VERIFIED -> R.string.sentinel_deep_links_status_not_verified_explanation
                VerificationStatus.NOT_REGISTERED -> R.string.sentinel_deep_links_status_not_registered_explanation
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val status =
            VerificationStatus.entries.firstOrNull { it.name == requireArguments().getString(KEY_STATUS) }
                ?: VerificationStatus.NOT_REGISTERED
        val builder =
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(labelOf(status))
                .setMessage(
                    getString(explanationOf(status)) + "\n\n" + getString(R.string.sentinel_deep_links_status_footer),
                ).setNegativeButton(R.string.sentinel_deep_links_status_close, null)
        if (status == VerificationStatus.NOT_VERIFIED) {
            builder.setPositiveButton(R.string.sentinel_deep_links_menu_open_link_settings) { _, _ ->
                parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(RESULT_OPEN_LINK_SETTINGS to true))
            }
        }
        return builder.create()
    }
}
