package com.infinum.sentinel.ui.bundles

import androidx.lifecycle.viewModelScope
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import com.infinum.sentinel.domain.bundle.shared.BundlesParameters
import com.infinum.sentinel.ui.bundles.callbacks.BundleCallSite
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject

@Inject
internal class BundlesViewModel(
    private val bundleMonitor: Repositories.BundleMonitor,
    private val bundles: Repositories.Bundles,
) : BaseChildViewModel<Nothing, BundlesEvent>() {
    private var parameters: BundlesParameters? =
        BundlesParameters(
            monitor = BundleMonitorParameters(),
            details = BundleParameters(),
        )

    override fun data() {
        bundleMonitor
            .load(parameters?.monitor ?: throw NullPointerException("Monitor cannot be null."))
            .combine(
                bundles.load(parameters?.details ?: throw NullPointerException("Details cannot be null.")),
            ) { monitor, descriptors ->
                descriptors
                    .map { it.copy(limit = monitor.limit) }
                    .filter {
                        when (it.callSite) {
                            BundleCallSite.ACTIVITY_INTENT_EXTRAS -> monitor.activityIntentExtras
                            BundleCallSite.ACTIVITY_SAVED_STATE -> monitor.activitySavedState
                            BundleCallSite.FRAGMENT_ARGUMENTS -> monitor.fragmentArguments
                            BundleCallSite.FRAGMENT_SAVED_STATE -> monitor.fragmentSavedState
                        }
                    }.filter {
                        it.className?.lowercase()?.contains(
                            parameters
                                ?.monitor
                                ?.query
                                ?.lowercase()
                                .orEmpty(),
                        ) == true
                    }
            }.flowOn(runningDispatchers)
            .onEach { emitEvent(BundlesEvent.BundlesIntercepted(value = it)) }
            .launchIn(viewModelScope)
    }

    fun clearBundles() =
        launch {
            io {
                bundles.clear()
            }
        }

    fun setSearchQuery(query: String?) {
        parameters =
            parameters?.copy(
                monitor = parameters?.monitor?.copy(query = query) ?: BundleMonitorParameters(),
            )
        data()
    }
}
