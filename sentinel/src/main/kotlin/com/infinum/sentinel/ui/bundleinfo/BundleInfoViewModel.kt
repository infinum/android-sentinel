package com.infinum.sentinel.ui.bundleinfo

import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import com.infinum.sentinel.domain.triggers.models.TriggerParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn

internal class BundleInfoViewModel(
//    private val triggers: Repositories.Triggers,
//    private val formats: Repositories.Formats
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
//
//    fun toggleTrigger(entity: TriggerEntity) {
//        launch {
//            io {
//                triggers.save(
//                    TriggerParameters(
//                        entity = entity
//                    )
//                )
//            }
//        }
//    }
//
//    fun saveFormats(entities: List<FormatEntity>) {
//        launch {
//            io {
//                formats.save(
//                    FormatsParameters(
//                        entities = entities
//                    )
//                )
//            }
//        }
//    }
}
