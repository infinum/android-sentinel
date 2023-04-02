package com.infinum.sentinel.ui.shared.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseChildFragment<State, Event>(
    @LayoutRes contentLayoutId: Int
) : Fragment(contentLayoutId), BaseView<State, Event> {

    abstract val binding: ViewBinding

    abstract override val viewModel: BaseChildViewModel<State, Event>

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        collectFlows(viewLifecycleOwner)

        viewModel.data()
    }
}
