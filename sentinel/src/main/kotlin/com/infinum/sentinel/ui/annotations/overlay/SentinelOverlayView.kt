package com.infinum.sentinel.ui.annotations.overlay

/**
 * Marks a `View` as part of Sentinel's own chrome layered on top of the host app — an overlay
 * this library adds directly to `decorView`, never something the host app or its user put there.
 *
 * [ViewTargetNode] filters out every child that implements this before it ever builds a node for
 * it, so a Sentinel overlay never appears in the resolved tree at all. That matters beyond just
 * excluding it from selection: an overlay such as [AnnotationOverlayView] is added as the last
 * `decorView` child and covers the whole screen, so without this filter it would be the
 * *deepest* hit for every tap — it has no children of its own to descend into — and
 * `deepestAt` would stop there before ever reaching the real content underneath. Being listed in
 * `CHROME_TYPES` only suppresses annotatability; it does nothing to stop traversal from ending at
 * a childless node that covers the tap point.
 */
internal interface SentinelOverlayView
