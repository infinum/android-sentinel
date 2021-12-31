package com.infinum.sentinel.ui.main.device

import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class DeviceViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<DeviceState, Nothing>() {

    override fun data() =
        launch {
            val result = io {
                collectors.device()()
            }
            setState(
                DeviceState.Data(
                    value = result
                )
            )
        }
}
