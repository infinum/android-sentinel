package com.infinum.sentinel.domain.deeplinks.models

/**
 * A screen (activity or alias) with the base links it declares.
 */
internal data class DeclaredScreen(
    val componentName: String,
    val title: String,
    val links: List<BaseLink>,
)
