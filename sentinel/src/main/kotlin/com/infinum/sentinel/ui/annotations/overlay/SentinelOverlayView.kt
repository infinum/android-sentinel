package com.infinum.sentinel.ui.annotations.overlay

/**
 * Marks a `View` as Sentinel's own chrome, added by this library directly to `decorView` on top
 * of the host app.
 *
 * [ViewTargetNode] leaves these views out of the resolved tree entirely. An overlay such as
 * [AnnotationOverlayView] is the last `decorView` child and covers the screen, so if it stayed in
 * the tree, `deepestAt` would stop at it instead of reaching the content underneath.
 */
internal interface SentinelOverlayView
