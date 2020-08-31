package com.infinum.sentinel.ui.shared

import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseChildFragment(
    @LayoutRes contentLayoutId: Int
) : Fragment(contentLayoutId) {

    abstract val binding: ViewBinding
}
