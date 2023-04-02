package com.infinum.sentinel.ui.main.permissions

import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import me.tatarka.inject.annotations.Inject

@Inject
internal class PermissionsViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<PermissionsState, Nothing>() {

    override fun data() =
        launch {
            val result = io {
                collectors.permissions()()
            }
            setState(
                PermissionsState.Data(
                    value = result
                )
            )
        }
}
