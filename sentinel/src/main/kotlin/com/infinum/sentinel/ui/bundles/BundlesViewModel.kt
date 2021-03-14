package com.infinum.sentinel.ui.bundles

import androidx.lifecycle.viewModelScope
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import com.infinum.sentinel.ui.bundles.callbacks.BundleCallSite
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class BundlesViewModel(
    private val bundleMonitor: Repositories.BundleMonitor,
    private val bundles: Repositories.Bundles
) : BaseChildViewModel<List<BundleDescriptor>>() {

    override fun data(action: (List<BundleDescriptor>) -> Unit) =
        launch {
            bundleMonitor.load(BundleMonitorParameters())
                .combine(
                    bundles.load(BundleParameters())
                ) { monitor, descriptors ->
                    descriptors.map { it.copy(limit = monitor.limit) }.filter {
                        when (it.callSite) {
                            BundleCallSite.ACTIVITY_INTENT_EXTRAS -> monitor.activityIntentExtras
                            BundleCallSite.ACTIVITY_SAVED_STATE -> monitor.activitySavedState
                            BundleCallSite.FRAGMENT_ARGUMENTS -> monitor.fragmentArguments
                            BundleCallSite.FRAGMENT_SAVED_STATE -> monitor.fragmentSavedState
                        }
                    }
                }
                .flowOn(dispatchersIo)
                .onEach { action(it) }
                .launchIn(viewModelScope)
        }

    fun clearBundles() {
        launch {
            io {
                bundles.clear()
            }
        }
    }
}
