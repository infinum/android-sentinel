package com.infinum.sentinel.ui.main.permissions

import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.shared.base.BaseParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class PermissionsViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<BaseParameters, Map<String, Boolean>>() {

    override var parameters: BaseParameters? = null

    override fun data(action: (Map<String, Boolean>) -> Unit) =
        launch {
            val result = io {
                collectors.permissions()()
            }
            action(result)
        }
}
