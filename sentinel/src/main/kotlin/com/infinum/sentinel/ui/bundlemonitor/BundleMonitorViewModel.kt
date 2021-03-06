package com.infinum.sentinel.ui.bundlemonitor

import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class BundleMonitorViewModel(
    private val bundleMonitor: Repositories.BundleMonitor
) : BaseChildViewModel<Any>() {

    override fun data(action: (Any) -> Unit) = throw NotImplementedError()
//
//    fun triggers(action: (List<TriggerEntity>) -> Unit) =
//        launch {
//            triggers.load(TriggerParameters())
//                .flowOn(Dispatchers.IO)
//                .collectLatest {
//                    action(it)
//                }
//        }
//
//    fun formats(action: (FormatEntity) -> Unit) =
//        launch {
//            formats.load(FormatsParameters())
//                .flowOn(Dispatchers.IO)
//                .collectLatest {
//                    action(it)
//                }
//        }
}
