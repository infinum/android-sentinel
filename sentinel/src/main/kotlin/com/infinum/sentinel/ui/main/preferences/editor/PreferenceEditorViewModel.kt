package com.infinum.sentinel.ui.main.preferences.editor

import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.preference.models.PreferenceParameters
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel

internal class PreferenceEditorViewModel(
    private val repository: Repositories.Preference
) : BaseChildViewModel<PreferenceEditorState, PreferenceEditorEvent>() {

    override fun data() =
        launch {
            val result = io {
                repository.consume()
            }
            setState(
                PreferenceEditorState.Cache(
                    name = result.first,
                    type = result.second.first,
                    key = result.second.second,
                    value = result.second.third
                )
            )
        }

    fun saveBoolean(fileName: String, key: String, currentValue: Boolean?, newValue: Boolean?) =
        launch {
            val result = io {
                if (currentValue == newValue) {
                    true
                } else {
                    if (currentValue != null && newValue != null) {
                        repository.save(
                            PreferenceParameters.BooleanType(
                                name = fileName,
                                key = key,
                                value = newValue
                            )
                        )
                        true
                    } else {
                        false
                    }
                }
            }
            if (result) {
                emitEvent(PreferenceEditorEvent.Saved())
            } else {
                setError(IllegalStateException("Preference paramaters are invalid."))
            }
        }

    fun saveFloat(fileName: String, key: String, currentValue: Float?, newValue: Float?) =
        launch {
            val result = io {
                if (currentValue == newValue) {
                    true
                } else {
                    if (currentValue != null && newValue != null) {
                        repository.save(
                            PreferenceParameters.FloatType(
                                name = fileName,
                                key = key,
                                value = newValue
                            )
                        )
                        true
                    } else {
                        false
                    }
                }
            }
            if (result) {
                emitEvent(PreferenceEditorEvent.Saved())
            } else {
                setError(IllegalStateException("Preference paramaters are invalid."))
            }
        }

    fun saveInteger(fileName: String, key: String, currentValue: Int?, newValue: Int?) =
        launch {
            val result = io {
                if (currentValue == newValue) {
                    true
                } else {
                    if (currentValue != null && newValue != null) {
                        repository.save(
                            PreferenceParameters.IntType(
                                name = fileName,
                                key = key,
                                value = newValue
                            )
                        )
                        true
                    } else {
                        false
                    }
                }
            }
            if (result) {
                emitEvent(PreferenceEditorEvent.Saved())
            } else {
                setError(IllegalStateException("Preference paramaters are invalid."))
            }
        }

    fun saveLong(fileName: String, key: String, currentValue: Long?, newValue: Long?) =
        launch {
            val result = io {
                if (currentValue == newValue) {
                    true
                } else {
                    if (currentValue != null && newValue != null) {
                        repository.save(
                            PreferenceParameters.LongType(
                                name = fileName,
                                key = key,
                                value = newValue
                            )
                        )
                        true
                    } else {
                        false
                    }
                }
            }
            if (result) {
                emitEvent(PreferenceEditorEvent.Saved())
            } else {
                setError(IllegalStateException("Preference paramaters are invalid."))
            }
        }

    fun saveString(fileName: String, key: String, currentValue: String?, newValue: String?) =
        launch {
            val result = io {
                if (currentValue == newValue) {
                    true
                } else {
                    if (currentValue != null && newValue != null) {
                        repository.save(
                            PreferenceParameters.StringType(
                                name = fileName,
                                key = key,
                                value = newValue
                            )
                        )
                        true
                    } else {
                        false
                    }
                }
            }
            if (result) {
                emitEvent(PreferenceEditorEvent.Saved())
            } else {
                setError(IllegalStateException("Preference paramaters are invalid."))
            }
        }

    fun saveArray(fileName: String, key: String, currentValue: Array<String>?, newValue: Array<String>?) =
        launch {
            val result = io {
                if (currentValue.contentEquals(newValue)) {
                    true
                } else {
                    if (currentValue != null && newValue != null) {
                        repository.save(
                            PreferenceParameters.ArrayType(
                                name = fileName,
                                key = key,
                                value = newValue
                            )
                        )
                        true
                    } else {
                        false
                    }
                }
            }
            if (result) {
                emitEvent(PreferenceEditorEvent.Saved())
            } else {
                setError(IllegalStateException("Preference paramaters are invalid."))
            }
        }
}
