package com.infinum.sentinel.ui.shared.base

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.infinum.sentinel.R

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseFragment<State, Event>(
    @LayoutRes private val contentLayoutId: Int
) : DialogFragment(), BaseView<State, Event> {

    abstract val binding: ViewBinding

    override fun getTheme(): Int = R.style.Sentinel_Theme_Dialog

    @CallSuper
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window?.let {
                    it.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    it.attributes.blurBehindRadius =
                        resources.getDimensionPixelSize(R.dimen.sentinel_blur_radius)
                }
            }
            window?.let {
                val layoutParams: WindowManager.LayoutParams = it.attributes
                layoutParams.gravity = Gravity.BOTTOM
                it.attributes = layoutParams
            }
        }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        when (contentLayoutId != 0) {
            true -> inflater.inflate(contentLayoutId, container, false)
            false -> null
        }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        collectFlows(viewLifecycleOwner)
    }

    @CallSuper
    override fun onDetach() =
        super.onDetach().run {
            requireActivity().finish()
        }
}
