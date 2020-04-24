package com.infinum.sentinel.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.infinum.sentinel.R

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseFragment<Binding : ViewBinding> : BottomSheetDialogFragment() {

    private var binding: Binding? = null

    internal val viewBinding get() = binding!!

    override fun getTheme(): Int = R.style.Sentinel_Theme_BottomSheet

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog =
        BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = provideViewBinding(inflater, container)
        return viewBinding.root
    }

    override fun onDestroy() =
        super.onDestroy().run {
            binding = null
        }

    override fun onDetach() =
        super.onDetach().run {
            requireActivity().finish()
        }

    protected abstract fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): Binding
}
