package com.infinum.sentinel.ui.main.preferences.editor

import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class PreferenceEditorViewModel(
    private val collectors: Factories.Collector
) : BaseChildViewModel<Any, Any>() {

    override fun data() = Unit

    fun data(fileName: String?, key: String?, type: PreferenceType?, value: Any?) {
//        println("_BOJAN_ -> fileName: $fileName, key: $key, type: $type, value: $value")
        launch {
            io {
                collectors.preferences()()
            }
        }
    }
}
