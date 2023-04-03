package com.infinum.sentinel.ui.bundles.details

import androidx.lifecycle.viewModelScope
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject

@Inject
internal class BundleDetailsViewModel(
    private val bundles: Repositories.Bundles
) : BaseChildViewModel<BundleDetailsState, Nothing>() {

    private var parameters: BundleParameters = BundleParameters()

    override fun data() =
        launch {
            bundles.load(parameters)
                .flowOn(runningDispatchers)
                .map { it.single { descriptor -> descriptor.bundleTree.id == parameters.bundleId } }
                .onEach { setState(BundleDetailsState.Data(value = it)) }
                .launchIn(viewModelScope)
        }

    fun setBundleId(value: String?) {
        parameters = parameters.copy(bundleId = value)
    }
}
