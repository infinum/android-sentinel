package com.infinum.sentinel.ui.annotations.overlay

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AnnotationInspectCardTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private class FakeNode(
        override val type: String,
        override val bounds: Bounds,
        override val text: String? = null,
        override val viewId: String? = null,
        override val sourceLocation: String? = null,
    ) : TargetNode {
        override val testTag: String? = null
        override val annotatable: Boolean = true
        override val children: List<TargetNode> = emptyList()
        override val parent: TargetNode? = null
    }

    @Test
    @JvmName("theCardShowsTheTargetType")
    fun `the card shows the target type`() {
        val card = AnnotationInspectCard(context)

        card.showFor(FakeNode("MaterialButton", Bounds(0, 0, 100, 40)), Bounds(0, 0, 1080, 1920))

        assertEquals("MaterialButton", card.typeText())
        assertEquals(View.VISIBLE, card.visibility)
    }

    @Test
    @JvmName("theCardShowsTheResourceIdWhenOneResolved")
    fun `the card shows the resource id when one resolved`() {
        val card = AnnotationInspectCard(context)

        card.showFor(
            FakeNode("MaterialButton", Bounds(0, 0, 100, 40), viewId = "submitButton"),
            Bounds(0, 0, 1080, 1920),
        )

        assertTrue(card.detailText().contains("submitButton"))
    }

    @Test
    @JvmName("theCardPrefersTheSourceLocationOverTheResourceId")
    fun `the card prefers the source location over the resource id`() {
        val card = AnnotationInspectCard(context)

        card.showFor(
            FakeNode("PrimaryButton", Bounds(0, 0, 100, 40), viewId = "x", sourceLocation = "Checkout.kt:12"),
            Bounds(0, 0, 1080, 1920),
        )

        assertTrue(card.detailText().contains("Checkout.kt:12"))
    }

    @Test
    @JvmName("hideRemovesTheCardFromView")
    fun `hide removes the card from view`() {
        val card = AnnotationInspectCard(context)
        card.showFor(FakeNode("Button", Bounds(0, 0, 100, 40)), Bounds(0, 0, 1080, 1920))

        card.hide()

        assertEquals(View.GONE, card.visibility)
    }

    @Test
    @JvmName("addNoteReportsThroughTheCallback")
    fun `add note reports through the callback`() {
        var tapped = false
        val card = AnnotationInspectCard(context).apply { onAddNote = { tapped = true } }
        card.showFor(FakeNode("Button", Bounds(0, 0, 100, 40)), Bounds(0, 0, 1080, 1920))

        card.performAddNote()

        assertTrue(tapped)
    }
}
