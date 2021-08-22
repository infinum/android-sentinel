package com.infinum.sentinel.ui.shared.base

import com.infinum.sentinel.domain.shared.base.BaseParameters

internal abstract class BaseChildViewModel<Parameters : BaseParameters, Data> : BaseViewModel() {

    abstract var parameters: Parameters?

    abstract fun data(action: (Data) -> Unit)
}
