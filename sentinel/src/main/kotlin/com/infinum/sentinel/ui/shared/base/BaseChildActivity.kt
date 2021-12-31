package com.infinum.sentinel.ui.shared.base

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseChildActivity : BaseActivity<Nothing, Nothing>() {

    override val viewModel: BaseViewModel<Nothing, Nothing>? = null

    override fun onState(state: Nothing) = Unit

    override fun onEvent(event: Nothing) = Unit
}
