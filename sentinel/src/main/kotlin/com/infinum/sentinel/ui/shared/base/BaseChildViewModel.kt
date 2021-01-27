package com.infinum.sentinel.ui.shared.base

internal abstract class BaseChildViewModel<T> : BaseViewModel() {

    abstract fun data(action: (T) -> Unit)
}
