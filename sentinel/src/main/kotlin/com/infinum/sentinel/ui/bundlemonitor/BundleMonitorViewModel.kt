package com.infinum.sentinel.ui.bundlemonitor

import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

internal class BundleMonitorViewModel(
    private val bundleMonitor: Repositories.BundleMonitor,
    private val bundles: Repositories.Bundles
) : BaseChildViewModel<BundleDescriptor>() {

    override fun data(action: (BundleDescriptor) -> Unit) =
        launch {
            combine(
                bundleMonitor.load(BundleMonitorParameters())
                    .flowOn(dispatchersIo),
                bundles.load(BundleParameters())
                    .flowOn(dispatchersIo)
            ) { monitor, descriptor ->
                descriptor.copy(
                    magnitude = descriptor.bundleTree.size.toFloat() / (monitor.limit * 1000)
                )
            }
                .distinctUntilChanged()
                .collectLatest {
                    action(it)
                }
        }

    fun bundles(action: (BundleDescriptor) -> Unit) =
        launch {
            bundles.load(BundleParameters())
                .flowOn(dispatchersIo)
                .collectLatest {
                    action(it)
                }
        }
}
