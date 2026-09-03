package com.infinum.sentinel.ui.annotations.overlay

import android.content.res.Resources
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

/**
 * Adapts an Android [View] subtree to [TargetNode].
 *
 * Built eagerly on tap. Invisible and zero-size views are dropped, so what the tree contains
 * is what the user can actually see. A Compose host is walked as a leaf — see [children] — so
 * this doubles as the fallback used when Compose's own resolver cannot read the composition.
 *
 * [isOpaqueHost] defaults to [ComposeSupport.isComposeHost] and exists as a constructor
 * parameter, rather than a direct call, purely so tests can stand in a plain `ViewGroup` for a
 * Compose host: Compose is `compileOnly` for this module, so the real detection can never fire
 * where there is no real `AndroidComposeView` on the classpath to find.
 */
internal class ViewTargetNode(
    val view: View,
    override val parent: TargetNode?,
    private val isOpaqueHost: (View) -> Boolean = ComposeSupport::isComposeHost,
) : TargetNode {
    override val type: String = view.javaClass.simpleName

    override val viewId: String? = resolveViewId(view)

    override val testTag: String? = null

    override val sourceLocation: String? = null

    /**
     * The element's live visible text — what the user is looking at right now, not a static
     * label from the layout. A password field is the one exception: [isPasswordInput]. Its
     * `contentDescription`, being developer-authored, is still safe to fall through to.
     */
    override val text: String? =
        when {
            view is TextView && !isPasswordInput(view) && view.text.isNotEmpty() -> view.text.toString()
            view.contentDescription != null -> view.contentDescription.toString()
            else -> null
        }

    override val bounds: Bounds = boundsOf(view)

    /**
     * A view is worth selecting when the app declared its id, when it draws content, or when
     * it is interactive. Framework scaffolding is excluded outright.
     */
    override val annotatable: Boolean =
        !isFrameworkChrome(view) &&
            (
                viewId != null ||
                    view is TextView ||
                    view is ImageView ||
                    view is EditText ||
                    view is Checkable ||
                    view.isClickable
                )

    /**
     * Children that are Sentinel's own overlays (see [SentinelOverlayView]) are dropped before a
     * node is ever built for them, not merely marked non-annotatable: an overlay such as
     * `AnnotationOverlayView` is the last `decorView` child and covers the whole screen, so
     * leaving it in the tree would make it the deepest hit for every tap — it has no children of
     * its own — before the real content underneath it is ever considered.
     *
     * A Compose host (see [ComposeSupport.isComposeHost], reached here through [isOpaqueHost]) is
     * a different case: its own node stays in the tree so it can be selected, but this walk does
     * not descend into it. Compose's internal interop views — `AndroidViewsHandler` and whatever
     * else lives under an `AndroidComposeView` — are meaningless to a coding agent, so treating
     * the host as a leaf keeps them out of the tree entirely rather than letting one of them win
     * as the "deepest" hit when Compose's own resolver cannot read the composition.
     */
    override val children: List<TargetNode> =
        if (view is ViewGroup && !isOpaqueHost(view)) {
            (0 until view.childCount)
                .map { view.getChildAt(it) }
                .filter { it.visibility == View.VISIBLE && it.width > 0 && it.height > 0 && it !is SentinelOverlayView }
                .map { ViewTargetNode(it, this, isOpaqueHost) }
        } else {
            emptyList()
        }

    private companion object {
        /**
         * `AnnotationOverlayView` (and any other [SentinelOverlayView]) is deliberately absent
         * here: [children] drops those before a node is ever built for one, which makes this
         * list redundant for them — nothing in this codebase ever builds a [ViewTargetNode] for
         * a Sentinel overlay by any other path, so there is no node left for this to catch.
         */
        private val CHROME_TYPES =
            setOf(
                "DecorView",
                "ActionBarOverlayLayout",
                "ContentFrameLayout",
                "ActionBarContainer",
                "ViewStubCompat",
                "FitWindowsLinearLayout",
            )

        /**
         * Whether [view] is a password entry field, whose live contents must never be captured.
         *
         * `EditText` extends `TextView`, so without this the export payload would carry whatever
         * the user has typed into the host app's form — and [com.infinum.sentinel.annotations
         * .ClaudeRoutineExporter] POSTs that payload to an external endpoint.
         *
         * The class bits have to be checked alongside the variation bits: `TYPE_NUMBER_VARIATION_
         * PASSWORD` and `TYPE_TEXT_VARIATION_URI` are the same value, and only the class tells
         * them apart.
         */
        private fun isPasswordInput(view: View): Boolean {
            if (view !is EditText) return false
            val inputClass = view.inputType and InputType.TYPE_MASK_CLASS
            val variation = view.inputType and InputType.TYPE_MASK_VARIATION
            return when (inputClass) {
                InputType.TYPE_CLASS_TEXT ->
                    variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
                InputType.TYPE_CLASS_NUMBER -> variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
                else -> false
            }
        }

        private fun isFrameworkChrome(view: View): Boolean =
            view.javaClass.simpleName in CHROME_TYPES || view.javaClass.name.startsWith("com.android.internal")

        /**
         * The resource entry name of the view's id when the app declared it. A library's or the
         * framework's ids are ignored: only the app's own ids are greppable in the app's source.
         */
        private fun resolveViewId(view: View): String? {
            val id = view.id
            if (id == View.NO_ID) return null
            return try {
                if (view.resources.getResourcePackageName(id) != view.context.packageName) {
                    null
                } else {
                    view.resources.getResourceEntryName(id)
                }
            } catch (ignored: Resources.NotFoundException) {
                null
            }
        }

        private fun boundsOf(view: View): Bounds {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            return Bounds(location[0], location[1], location[0] + view.width, location[1] + view.height)
        }
    }
}

internal fun View.toTargetNode(
    parent: TargetNode?,
    isOpaqueHost: (View) -> Boolean = ComposeSupport::isComposeHost,
): ViewTargetNode = ViewTargetNode(this, parent, isOpaqueHost)
