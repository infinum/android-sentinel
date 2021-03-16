package com.infinum.sentinel.ui.bundles.details

import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class BundleDetailsViewModel(
    private val bundles: Repositories.Bundles
) : BaseChildViewModel<BundleParameters, BundleDescriptor>() {

    override var parameters: BundleParameters? = BundleParameters()

    override fun data(action: (BundleDescriptor) -> Unit) =
        launch {
            bundles.load(parameters!!)
                .flowOn(dispatchersIo)
                .map { it.single { descriptor -> descriptor.bundleTree.id == parameters?.bundleId } }
                .onEach { action(it) }
                .launchIn(this)
        }

    fun setBundleId(value: String?) {
        parameters = parameters?.copy(bundleId = value)
    }
}
