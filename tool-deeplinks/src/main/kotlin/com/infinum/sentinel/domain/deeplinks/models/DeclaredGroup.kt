package com.infinum.sentinel.domain.deeplinks.models

/**
 * A `uri-relative-filter-group` (Android 15+). Only groups with [isAllowed] true add matching paths.
 */
internal data class DeclaredGroup(
    val isAllowed: Boolean,
    val data: List<DeclaredData>,
)
