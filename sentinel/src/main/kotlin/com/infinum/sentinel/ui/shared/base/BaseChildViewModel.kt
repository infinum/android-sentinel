package com.infinum.sentinel.ui.shared.base

internal abstract class BaseChildViewModel<State, Event> : BaseViewModel<State, Event>() {

    abstract fun data()
}
