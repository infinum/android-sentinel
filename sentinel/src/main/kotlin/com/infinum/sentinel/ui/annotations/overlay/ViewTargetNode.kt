package com.infinum.sentinel.ui.annotations.overlay

import android.content.res.Resources
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
 * is what the user can actually see.
 */
internal class ViewTargetNode(
    val view: View,
    override val parent: TargetNode?,
) : TargetNode {
    override val type: String = view.javaClass.simpleName

    override val viewId: String? = resolveViewId(view)

    override val testTag: String? = null

    override val sourceLocation: String? = null

    override val text: String? =
        when {
            view is TextView && view.text.isNotEmpty() -> view.text.toString()
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

    override val children: List<TargetNode> =
        if (view is ViewGroup) {
            (0 until view.childCount)
                .map { view.getChildAt(it) }
                .filter { it.visibility == View.VISIBLE && it.width > 0 && it.height > 0 }
                .map { ViewTargetNode(it, this) }
        } else {
            emptyList()
        }

    private companion object {
        private val CHROME_TYPES =
            setOf(
                "DecorView",
                "ActionBarOverlayLayout",
                "ContentFrameLayout",
                "ActionBarContainer",
                "ViewStubCompat",
                "FitWindowsLinearLayout",
                "AnnotationOverlayView",
            )

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

internal fun View.toTargetNode(parent: TargetNode?): ViewTargetNode = ViewTargetNode(this, parent)
