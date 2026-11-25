package com.infinum.sentinel.domain.deeplinks.models

/**
 * One VIEW intent filter of a component, reduced to what link discovery needs. [isEnabled] is the component's
 * `android:enabled`, replaced by its runtime state when the app changed it.
 */
internal data class DeclaredFilter(
    val componentName: String,
    val title: String,
    val categories: List<String>,
    val data: List<DeclaredData>,
    val groups: List<DeclaredGroup> = listOf(),
    val isEnabled: Boolean = true,
)
