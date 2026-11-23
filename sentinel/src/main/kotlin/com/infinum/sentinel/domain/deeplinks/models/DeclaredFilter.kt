package com.infinum.sentinel.domain.deeplinks.models

/**
 * One VIEW intent filter of a component, reduced to what link discovery needs.
 */
internal data class DeclaredFilter(
    val componentName: String,
    val title: String,
    val categories: List<String>,
    val data: List<DeclaredData>,
    val groups: List<DeclaredGroup> = listOf(),
)
