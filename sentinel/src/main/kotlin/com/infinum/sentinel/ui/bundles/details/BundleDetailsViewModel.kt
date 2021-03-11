package com.infinum.sentinel.ui.bundles.details

import androidx.lifecycle.viewModelScope
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
) : BaseChildViewModel<List<BundleDescriptor>>() {

    override fun data(action: (List<BundleDescriptor>) -> Unit) = throw NotImplementedError()

    fun bundleById(id: String, action: (BundleDescriptor) -> Unit) =
        launch {
            bundles.load(BundleParameters())
                .flowOn(dispatchersIo)
                .map { it.single { descriptor -> descriptor.bundleTree.id == id } }
                .onEach { action(it) }
                .launchIn(viewModelScope)
        }
}
