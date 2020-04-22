package com.infinum.sentinel.ui.shared

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.infinum.sentinel.R

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.Sentinel_Theme_BottomSheet

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog =
        BottomSheetDialog(requireContext(), theme)
}
