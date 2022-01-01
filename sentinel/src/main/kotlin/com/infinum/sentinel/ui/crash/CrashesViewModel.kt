package com.infinum.sentinel.ui.crash

import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class CrashesViewModel : BaseChildViewModel<Nothing, Nothing>() {

    override fun data() = Unit

//    fun clearBundles() =
//        launch {
//            io {
//                bundles.clear()
//            }
//        }
//
//    fun setSearchQuery(query: String?) {
//        parameters = parameters?.copy(
//            monitor = parameters?.monitor?.copy(query = query) ?: BundleMonitorParameters()
//        )
//        data()
//    }
}
