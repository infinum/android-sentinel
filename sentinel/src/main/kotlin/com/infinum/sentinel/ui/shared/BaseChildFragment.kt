package com.infinum.sentinel.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseChildFragment<Binding : ViewBinding> : Fragment() {

    private var binding: Binding? = null

    internal val viewBinding: Binding by lazy { binding!! }

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

    protected abstract fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): Binding
}
