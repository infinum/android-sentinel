package com.infinum.sentinel.ui.annotations.overlay

import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ViewTargetResolverTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun laidOut(root: FrameLayout, width: Int = 200, height: Int = 200): FrameLayout {
        root.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY),
        )
        root.layout(0, 0, width, height)
        return root
    }

    /**
     * An annotation capturing [captured], overridable per test so each `rebind` scenario can
     * diverge from what was actually captured (a different [targetType] or [viewId]) without
     * repeating every other field.
     */
    private fun annotationFor(
        captured: ResolvedTarget,
        rebindX: Int,
        rebindY: Int,
        targetType: String = captured.node.type,
        viewId: String? = captured.node.viewId,
        text: String? = captured.node.text,
    ): Annotation =
        Annotation(
            id = "test-annotation",
            note = "note",
            toolkit = AnnotationToolkit.VIEW,
            targetType = targetType,
            targetPath = TargetSelection.pathOf(captured.node),
            viewId = viewId,
            text = text,
            rebindX = rebindX,
            rebindY = rebindY,
        )

    @Test
    @JvmName("aTextViewIsAnnotatableAndReportsItsText")
    fun `a text view is annotatable and reports its text`() {
        val text = TextView(context).apply { setText("Submit") }
        val root = laidOut(FrameLayout(context).apply { addView(text) })

        val node = root.toTargetNode(parent = null)
        val leaf = TargetSelection.deepestAt(node, 10, 10)!!

        assertEquals("TextView", leaf.type)
        assertEquals("Submit", leaf.text)
        assertTrue(leaf.annotatable)
    }

    @Test
    @JvmName("aPlainContainerWithoutAnAppIdIsNotAnnotatable")
    fun `a plain container without an app id is not annotatable`() {
        val container = LinearLayout(context)
        val root = laidOut(FrameLayout(context).apply { addView(container) })

        val node = root.toTargetNode(parent = null)
        val child = node.children.first()

        assertEquals("LinearLayout", child.type)
        assertTrue(!child.annotatable)
    }

    @Test
    @JvmName("aClickableViewIsAnnotatable")
    fun `a clickable view is annotatable`() {
        val clickable = FrameLayout(context).apply { isClickable = true }
        val root = laidOut(FrameLayout(context).apply { addView(clickable) })

        assertTrue(root.toTargetNode(parent = null).children.first().annotatable)
    }

    @Test
    @JvmName("anInvisibleViewIsSkipped")
    fun `an invisible view is skipped`() {
        val hidden = TextView(context).apply { visibility = android.view.View.GONE }
        val root = laidOut(FrameLayout(context).apply { addView(hidden) })

        assertTrue(root.toTargetNode(parent = null).children.isEmpty())
    }

    @Test
    @JvmName("resolveReturnsTheButtonUnderThePointWithItsPath")
    fun `resolve returns the button under the point with its path`() {
        val button = Button(context).apply { setText("Go") }
        val root = laidOut(FrameLayout(context).apply { addView(button) })

        val resolved = ViewTargetResolver().resolve(root, 5, 5)!!

        assertEquals("Button", resolved.node.type)
        assertTrue(resolved.handle === button)
    }

    @Test
    @JvmName("resolveReturnsTheLastAddedChildWhenChildrenOverlap")
    fun `resolve returns the last-added child when children overlap`() {
        val below = Button(context).apply { setText("Below") }
        val above = Button(context).apply { setText("Above") }
        val root =
            laidOut(
                FrameLayout(context).apply {
                    addView(below, FrameLayout.LayoutParams(100, 100))
                    addView(above, FrameLayout.LayoutParams(100, 100))
                },
            )

        val resolved = ViewTargetResolver().resolve(root, 5, 5)!!

        assertTrue(resolved.handle === above)
    }

    @Test
    @JvmName("rebindReturnsTheTargetWhenTypeAndViewIdMatch")
    fun `rebind returns the target when type and viewId match`() {
        val button =
            Button(context).apply {
                id = com.infinum.sentinel.R.id.fab
                setText("Go")
            }
        val root = laidOut(FrameLayout(context).apply { addView(button) })
        val captured = ViewTargetResolver().resolve(root, 5, 5)!!
        val annotation = annotationFor(captured, rebindX = 5, rebindY = 5, viewId = "fab")

        val rebound = ViewTargetResolver().rebind(annotation, root)

        assertTrue(rebound != null)
        assertTrue(rebound!!.handle === button)
    }

    @Test
    @JvmName("rebindReturnsNullOnATypeMismatch")
    fun `rebind returns null on a type mismatch`() {
        val button =
            Button(context).apply {
                id = com.infinum.sentinel.R.id.fab
                setText("Go")
            }
        val root = laidOut(FrameLayout(context).apply { addView(button) })
        val captured = ViewTargetResolver().resolve(root, 5, 5)!!
        val annotation =
            annotationFor(captured, rebindX = 5, rebindY = 5, viewId = "fab", targetType = "TextView")

        val rebound = ViewTargetResolver().rebind(annotation, root)

        assertTrue(rebound == null)
    }

    @Test
    @JvmName("rebindReturnsNullOnAViewIdMismatch")
    fun `rebind returns null on a viewId mismatch`() {
        val button =
            Button(context).apply {
                id = com.infinum.sentinel.R.id.fab
                setText("Go")
            }
        val root = laidOut(FrameLayout(context).apply { addView(button) })
        val captured = ViewTargetResolver().resolve(root, 5, 5)!!
        val annotation = annotationFor(captured, rebindX = 5, rebindY = 5, viewId = "someOtherId")

        val rebound = ViewTargetResolver().rebind(annotation, root)

        assertTrue(rebound == null)
    }

    @Test
    @JvmName("rebindReturnsNullOnATextMismatchWhenTheTargetHasNoViewId")
    fun `rebind returns null on a text mismatch when the target has no viewId`() {
        // No viewId is set on this view, so rebind must fall back to matching by text. A
        // positive-only test here couldn't tell a real fallback from an implementation that
        // just accepts any candidate once viewId is null, so this asserts the rejection side:
        // a differing text must still fail the match.
        val text = TextView(context).apply { setText("Submit") }
        val root = laidOut(FrameLayout(context).apply { addView(text) })
        val captured = ViewTargetResolver().resolve(root, 5, 5)!!
        val annotation = annotationFor(captured, rebindX = 5, rebindY = 5, viewId = null, text = "Different")

        val rebound = ViewTargetResolver().rebind(annotation, root)

        assertTrue(rebound == null)
    }
}
