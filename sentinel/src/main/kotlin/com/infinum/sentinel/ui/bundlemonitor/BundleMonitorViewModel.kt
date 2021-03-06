package com.infinum.sentinel.ui.bundlemonitor

import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

internal class BundleMonitorViewModel(
    private val bundleMonitor: Repositories.BundleMonitor,
    private val bundles: Repositories.Bundles
) : BaseChildViewModel<Any>() {

    override fun data(action: (Any) -> Unit) = throw NotImplementedError()

    fun bundles(action: (BundleDescriptor) -> Unit) =
        launch {
            bundles.load(BundleParameters())
                .flowOn(dispatchersIo)
                .collectLatest {
                    action(it)
                }
        }
}
