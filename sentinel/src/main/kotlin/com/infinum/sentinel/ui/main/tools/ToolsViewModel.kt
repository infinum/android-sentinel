package com.infinum.sentinel.ui.main.tools

import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.shared.base.BaseParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class ToolsViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<BaseParameters, Set<Sentinel.Tool>>() {

    override var parameters: BaseParameters? = null

    override fun data(action: (Set<Sentinel.Tool>) -> Unit) =
        launch {
            val result = io {
                collectors.tools()()
            }
            action(result)
        }
}
