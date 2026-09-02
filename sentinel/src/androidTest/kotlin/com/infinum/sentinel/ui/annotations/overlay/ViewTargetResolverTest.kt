package com.infinum.sentinel.ui.annotations.overlay

import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
}
