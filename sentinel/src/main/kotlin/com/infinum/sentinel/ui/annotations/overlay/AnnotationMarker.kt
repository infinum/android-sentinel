package com.infinum.sentinel.ui.annotations.overlay

import com.infinum.sentinel.annotations.SentinelAnnotation

/**
 * A saved note resolved to its current on-screen position, ready to draw.
 *
 * [number] is the 1-based position in the cache, so a note keeps its number even while other
 * notes are hidden on another screen.
 */
internal data class AnnotationMarker(
    val number: Int,
    val bounds: Bounds,
    val annotation: SentinelAnnotation,
)
