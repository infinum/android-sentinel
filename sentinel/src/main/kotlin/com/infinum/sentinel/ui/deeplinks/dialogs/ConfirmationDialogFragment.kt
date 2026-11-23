package com.infinum.sentinel.ui.deeplinks.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Asks the user to confirm an action. Everything it shows comes from its arguments, so it survives
 * rotation and process death. Show it on the host's child fragment manager and listen with
 * `childFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner)`.
 *
 * Confirming sends [RESULT_CONFIRMED] and [RESULT_PAYLOAD] (the payload given to [newInstance], which tells
 * the host what was confirmed). Cancelling sends nothing.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ConfirmationDialogFragment : DialogFragment() {
    companion object {
        const val RESULT_CONFIRMED: String = "RESULT_CONFIRMED"
        const val RESULT_PAYLOAD: String = "RESULT_PAYLOAD"
        const val TAG: String = "ConfirmationDialogFragment"

        private const val KEY_REQUEST_KEY = "KEY_CONFIRMATION_REQUEST_KEY"
        private const val KEY_TITLE = "KEY_CONFIRMATION_TITLE"
        private const val KEY_MESSAGE = "KEY_CONFIRMATION_MESSAGE"
        private const val KEY_POSITIVE = "KEY_CONFIRMATION_POSITIVE"
        private const val KEY_NEGATIVE = "KEY_CONFIRMATION_NEGATIVE"
        private const val KEY_PAYLOAD = "KEY_CONFIRMATION_PAYLOAD"

        /**
         * Creates the dialog. The result is sent under [requestKey] and carries [payload] back when confirmed.
         */
        fun newInstance(
            requestKey: String,
            title: String,
            message: String,
            positive: String,
            negative: String,
            payload: String? = null,
        ): ConfirmationDialogFragment =
            ConfirmationDialogFragment().apply {
                arguments =
                    bundleOf(
                        KEY_REQUEST_KEY to requestKey,
                        KEY_TITLE to title,
                        KEY_MESSAGE to message,
                        KEY_POSITIVE to positive,
                        KEY_NEGATIVE to negative,
                        KEY_PAYLOAD to payload,
                    )
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(args.getString(KEY_TITLE))
            .setMessage(args.getString(KEY_MESSAGE))
            .setPositiveButton(args.getString(KEY_POSITIVE)) { _, _ ->
                parentFragmentManager.setFragmentResult(
                    args.getString(KEY_REQUEST_KEY).orEmpty(),
                    bundleOf(
                        RESULT_CONFIRMED to true,
                        RESULT_PAYLOAD to args.getString(KEY_PAYLOAD),
                    ),
                )
            }.setNegativeButton(args.getString(KEY_NEGATIVE), null)
            .create()
    }
}
