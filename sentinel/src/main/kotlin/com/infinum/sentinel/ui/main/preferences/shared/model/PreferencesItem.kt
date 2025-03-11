package com.infinum.sentinel.ui.main.preferences.shared.model

import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.data.models.raw.PreferencesData

internal sealed class PreferencesItem {

    data class Parent(
        val name: String,
        var isExpanded: Boolean
    ) : PreferencesItem()

    data class Child(
        val preferenceType: PreferenceType,
        val label: String,
        val value: Any,
        val parentName: String
    ) : PreferencesItem()
}

internal fun List<PreferencesData>.flatten(): List<PreferencesItem> = flatMap { data ->
    listOf(PreferencesItem.Parent(name = data.name, isExpanded = data.isExpanded)) +
        if (data.isExpanded) {
            data.values.map { (preferenceType, label, value) ->
                PreferencesItem.Child(preferenceType, label, value, parentName = data.name)
            }
        } else {
            emptyList()
        }
}
