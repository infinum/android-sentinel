package com.infinum.sentinel.ui.main.application

import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class ApplicationViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<ApplicationState, Nothing>() {

    override fun data() =
        launch {
            val result = io {
                collectors.application()()
            }
            setState(
                ApplicationState.Data(
                    value = result
                )
            )
        }
}
