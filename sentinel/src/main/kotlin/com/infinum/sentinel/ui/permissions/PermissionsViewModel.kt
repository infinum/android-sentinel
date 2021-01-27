package com.infinum.sentinel.ui.permissions

import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class PermissionsViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<Map<String, Boolean>>() {

    override fun data(action: (Map<String, Boolean>) -> Unit) =
        launch {
            val result = io {
                collectors.permissions()()
            }
            action(result)
        }
}