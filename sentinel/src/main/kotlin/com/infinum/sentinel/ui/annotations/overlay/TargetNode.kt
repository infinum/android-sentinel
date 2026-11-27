package com.infinum.sentinel.ui.annotations.overlay

/**
 * A toolkit-independent view of one annotatable element.
 *
 * `ViewTargetResolver` adapts an Android `View` to this, `ComposeTargetResolver` adapts a
 * Compose tooling `Group`. Every selection rule in [TargetSelection] is written against this
 * interface so it is shared by both and testable without a device.
 *
 * [children] must be ordered back to front: the last child draws on top.
 */
internal interface TargetNode {
    /** View class simple name, or the composable function name. */
    val type: String

    /** Resource entry name of the view id, when the element has one. */
    val viewId: String?

    /** `Modifier.testTag` value, when set. */
    val testTag: String?

    /** `file:line` of the composable, when tooling data resolved it. */
    val sourceLocation: String?

    /** Visible text or content description. */
    val text: String?

    /** Whether this element is worth selecting, as opposed to framework scaffolding. */
    val annotatable: Boolean

    /**
     * Whether broadening the selection may stop at this node.
     *
     * Defaults to [annotatable]; an implementation widens it when it has nodes that are worth
     * broadening *to* without being worth picking outright — layout containers, typically.
     */
    val broadenable: Boolean get() = annotatable

    val bounds: Bounds

    val children: List<TargetNode>

    val parent: TargetNode?
}
