package com.infinum.sentinel.ui.shared.base

import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.infinum.sentinel.di.LibraryKoinComponent

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseChildFragment(
    @LayoutRes contentLayoutId: Int
) : Fragment(contentLayoutId), LibraryKoinComponent {

    abstract val binding: ViewBinding

    abstract val viewModel: BaseChildViewModel<*, *>
}
