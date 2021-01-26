package com.infinum.sentinel.ui.shared

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.infinum.sentinel.R

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseFragment(
    @LayoutRes private val contentLayoutId: Int
) : DialogFragment() {

    abstract val binding: ViewBinding

    override fun getTheme(): Int = R.style.Sentinel_Theme_Dialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(requireContext(), theme).also {
            val window: Window? = it.window
            val layoutParams: WindowManager.LayoutParams? = window?.attributes
            layoutParams?.gravity = Gravity.BOTTOM
            window?.attributes = layoutParams
        }

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
