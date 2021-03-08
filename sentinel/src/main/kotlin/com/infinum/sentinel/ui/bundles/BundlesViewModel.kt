package com.infinum.sentinel.ui.bundles

import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class BundlesViewModel(
    private val bundleMonitor: Repositories.BundleMonitor,
    private val bundles: Repositories.Bundles
) : BaseChildViewModel<BundleDescriptor>() {

    override fun data(action: (BundleDescriptor) -> Unit) {
        launch {
            bundles.load(BundleParameters())
                .flowOn(dispatchersIo)
                .onEach { action(it) }
                .launchIn(this)
        }
//        launch {
//            combine(
//                bundleMonitor.load(BundleMonitorParameters())
//                    .flowOn(dispatchersIo),
//                bundles.load(BundleParameters())
//                    .flowOn(dispatchersIo)
//            ) { monitor, descriptor ->
//                descriptor.copy(
//                    limit = monitor.limit
//                )
//            }
//                .flowOn(dispatchersIo)
//                .onEach { action(it) }
//                .launchIn(this)
//        }
    }
}
