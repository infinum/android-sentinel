package com.infinum.sentinel.ui.annotations.overlay

import android.content.res.Resources
import android.os.Build
import android.text.InputType
import android.text.method.PasswordTransformationMethod
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
 * is what the user can actually see. A Compose host is walked as a leaf (see [children]), so
 * this doubles as the fallback used when Compose's own resolver cannot read the composition.
 *
 * [isOpaqueHost] defaults to [ComposeSupport.isComposeHost]. It is a parameter so tests, which
 * have no real `AndroidComposeView` on the classpath, can stand in a plain `ViewGroup` for it.
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
     * The element's live visible text, not a static label from the layout. A password field's
     * text is withheld (see [isPasswordInput]), but its developer-authored `contentDescription`
     * is still used.
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
     *
     * Containers are not included. This predicate decides the initial pick, and an anonymous
     * wrapper must not outrank an id-bearing ancestor or a Compose host. Broadening uses
     * [broadenable] instead.
     */
    override val annotatable: Boolean =
        !isFrameworkChrome(view) && (viewId != null || drawsContent(view) || view.isClickable)

    /**
     * Whether broadening the selection may stop here.
     *
     * Wider than [annotatable]: a layout container counts as a whole section, so repeated taps
     * can walk up through anonymous `LinearLayout` / `FrameLayout` wrappers that carry no id.
     *
     * A Compose host is excluded: it is walked as a leaf, so it is never an intermediate step.
     */
    override val broadenable: Boolean =
        annotatable || (view is ViewGroup && !isOpaqueHost(view) && !isFrameworkChrome(view))

    /**
     * Sentinel's own overlays (see [SentinelOverlayView]) are left out of the tree, so a
     * full-screen overlay never becomes the deepest hit for a tap.
     *
     * A Compose host (detected through [isOpaqueHost]) stays in the tree so it can be selected,
     * but the walk does not descend into it. Compose's internal interop views, such as
     * `AndroidViewsHandler`, mean nothing in the app's source.
     *
     * Children are listed in drawing order, as [TargetNode.children] requires: see [drawingOrder].
     */
    override val children: List<TargetNode> =
        if (view is ViewGroup && !isOpaqueHost(view)) {
            drawingOrder(view)
                .filter { it.visibility == View.VISIBLE && it.width > 0 && it.height > 0 && it !is SentinelOverlayView }
                .map { ViewTargetNode(it, this, isOpaqueHost) }
        } else {
            emptyList()
        }

    private companion object {
        /** [SentinelOverlayView]s are not listed here because [children] already drops them. */
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
         * [group]'s children back to front, the way the framework draws them: in custom drawing
         * order where the group defines one (readable from API 29), then by Z, so an elevated
         * child sorts after the siblings it covers. The Z sort is stable, like the framework's.
         */
        private fun drawingOrder(group: ViewGroup): List<View> {
            val count = group.childCount
            val ordered =
                (0 until count).map { position ->
                    val index =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            group.getChildDrawingOrder(position).takeIf { it in 0 until count } ?: position
                        } else {
                            position
                        }
                    group.getChildAt(index)
                }
            return ordered.sortedBy { it.z }
        }

        /**
         * Whether [view] shows a password, whose live contents must never be captured: masked
         * with a [PasswordTransformationMethod], or an `EditText` with a password input type.
         *
         * The class bits have to be checked alongside the variation bits: `TYPE_NUMBER_VARIATION_
         * PASSWORD` and `TYPE_TEXT_VARIATION_URI` are the same value, and only the class tells
         * them apart.
         */
        private fun isPasswordInput(view: TextView): Boolean {
            if (view.transformationMethod is PasswordTransformationMethod) return true
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

        /** `EditText` is covered as a `TextView`; `Checkable` covers check boxes, switches and radio buttons. */
        private fun drawsContent(view: View): Boolean = view is TextView || view is ImageView || view is Checkable

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
