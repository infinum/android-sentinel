package com.infinum.sentinel.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.infinum.sentinel.R

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseFragment(
    @LayoutRes private val contentLayoutId: Int
) : BottomSheetDialogFragment() {

    abstract val binding: ViewBinding

    override fun getTheme(): Int = R.style.Sentinel_Theme_BottomSheet

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog =
        BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        when (contentLayoutId != 0) {
            true -> inflater.inflate(contentLayoutId, container, false)
            false -> null
        }

    override fun onDetach() =
        super.onDetach().run {
            requireActivity().finish()
        }
}
