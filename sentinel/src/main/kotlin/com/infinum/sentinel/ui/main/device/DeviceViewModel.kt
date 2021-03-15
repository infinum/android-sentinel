package com.infinum.sentinel.ui.main.device

import com.infinum.sentinel.data.models.raw.DeviceData
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.shared.base.BaseParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class DeviceViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<BaseParameters, DeviceData>() {

    override var parameters: BaseParameters? = null

    override fun data(action: (DeviceData) -> Unit) =
        launch {
            val result = io {
                collectors.device()()
            }
            action(result)
        }
}
