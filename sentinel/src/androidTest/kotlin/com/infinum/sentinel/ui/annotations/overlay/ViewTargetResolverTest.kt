package com.infinum.sentinel.ui.annotations.overlay

import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinum.sentinel.annotations.AnnotationToolkit
import com.infinum.sentinel.annotations.SentinelAnnotation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    ): SentinelAnnotation =
        SentinelAnnotation(
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
    @JvmName("aPlainContainerWithoutAnAppIdIsNotTheInitialPickButCanBeBroadenedTo")
    fun `a plain container without an app id is not the initial pick but can be broadened to`() {
        val container = LinearLayout(context)
        val root = laidOut(FrameLayout(context).apply { addView(container) })

        val node = root.toTargetNode(parent = null)
        val child = node.children.first()

        assertEquals("LinearLayout", child.type)
        // Not worth picking outright — an id-bearing ancestor or a Compose host outranks it...
        assertTrue(!child.annotatable)
        // ...but broadening must be able to stop here, or a leaf under id-less wrappers has
        // nowhere to broaden to and tapping it again does nothing.
        assertTrue(child.broadenable)
    }

    @Test
    @JvmName("aLeafUnderIdlessContainersCanBroadenUpTheTree")
    fun `a leaf under id-less containers can broaden up the tree`() {
        // Mirrors a real screen: a button wrapped in anonymous layout containers, none of which
        // carries an app id.
        val leaf = Button(context).apply { setText("Go") }
        val inner = LinearLayout(context).apply { addView(leaf) }
        val outer = LinearLayout(context).apply { addView(inner) }
        val root = laidOut(FrameLayout(context).apply { addView(outer) })

        val selected = ViewTargetResolver().resolve(root, 5, 5)!!.node
        assertEquals("Button", selected.type)

        val first = TargetSelection.stepUp(selected)
        val second = first?.let { TargetSelection.stepUp(it) }

        assertEquals("LinearLayout", first?.type)
        assertTrue(first !== selected)
        assertEquals("LinearLayout", second?.type)
        assertTrue(second !== first)
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

    /**
     * `EditText` extends `TextView`, so the plain "is it a TextView" capture used to lift a
     * password field's live contents into the export payload — which `ClaudeRoutineExporter`
     * POSTs to an external endpoint. Every password variation the platform defines is covered,
     * including the number one, whose bit value collides with `TYPE_TEXT_VARIATION_URI`.
     */
    @Test
    @JvmName("aPasswordFieldsLiveTextIsNeverCaptured")
    fun `a password field's live text is never captured`() {
        val variations =
            listOf(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD,
            )

        for (type in variations) {
            val field =
                EditText(context).apply {
                    inputType = type
                    setText("hunter2")
                }
            val root = laidOut(FrameLayout(context).apply { addView(field) })

            val leaf = TargetSelection.deepestAt(root.toTargetNode(parent = null), 10, 10)!!

            assertEquals("EditText", leaf.type)
            assertNull("inputType 0x${Integer.toHexString(type)} leaked its text", leaf.text)
        }
    }

    /** The exclusion is scoped to passwords: an ordinary field still identifies itself by text. */
    @Test
    @JvmName("anOrdinaryTextFieldStillReportsItsText")
    fun `an ordinary text field still reports its text`() {
        val field =
            EditText(context).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                setText("me@example.com")
            }
        val root = laidOut(FrameLayout(context).apply { addView(field) })

        val leaf = TargetSelection.deepestAt(root.toTargetNode(parent = null), 10, 10)!!

        assertEquals("me@example.com", leaf.text)
    }

    /**
     * A password field's `contentDescription` is authored by the developer, never typed by the
     * user, so it stays available as a locator when the live text is withheld.
     */
    @Test
    @JvmName("aPasswordFieldStillFallsBackToItsContentDescription")
    fun `a password field still falls back to its content description`() {
        val field =
            EditText(context).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setText("hunter2")
                contentDescription = "Password"
            }
        val root = laidOut(FrameLayout(context).apply { addView(field) })

        val leaf = TargetSelection.deepestAt(root.toTargetNode(parent = null), 10, 10)!!

        assertEquals("Password", leaf.text)
    }
}
