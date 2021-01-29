package com.infinum.sentinel.ui.application

import com.infinum.sentinel.data.models.raw.ApplicationData
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class ApplicationViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<ApplicationData>() {

    override fun data(action: (ApplicationData) -> Unit) =
        launch {
            val result = io {
                collectors.application()()
            }
            action(result)
        }
}
