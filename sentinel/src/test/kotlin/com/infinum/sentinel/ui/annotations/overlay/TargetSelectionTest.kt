package com.infinum.sentinel.ui.annotations.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class TargetSelectionTest {
    private class FakeNode(
        override val type: String,
        override val bounds: Bounds,
        override val annotatable: Boolean = true,
        override val viewId: String? = null,
        override val testTag: String? = null,
        override val sourceLocation: String? = null,
        override val text: String? = null,
        children: List<FakeNode> = emptyList(),
    ) : TargetNode {
        override var parent: TargetNode? = null
        override val children: List<TargetNode> = children

        init {
            children.forEach { it.parent = this }
        }
    }

    @Test
    fun `deepestAt returns the innermost node containing the point`() {
        val inner = FakeNode("Button", Bounds(10, 10, 50, 50))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(inner))

        assertEquals(inner, TargetSelection.deepestAt(root, 20, 20))
    }

    @Test
    fun `deepestAt returns the root when no child contains the point`() {
        val inner = FakeNode("Button", Bounds(10, 10, 50, 50))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(inner))

        assertEquals(root, TargetSelection.deepestAt(root, 80, 80))
    }

    @Test
    fun `deepestAt returns null when the point is outside the root`() {
        val root = FakeNode("Root", Bounds(0, 0, 100, 100))

        assertNull(TargetSelection.deepestAt(root, 200, 200))
    }

    @Test
    fun `deepestAt prefers the last overlapping sibling because it draws on top`() {
        val below = FakeNode("Below", Bounds(0, 0, 50, 50))
        val above = FakeNode("Above", Bounds(0, 0, 50, 50))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(below, above))

        assertEquals(above, TargetSelection.deepestAt(root, 10, 10))
    }

    @Test
    fun `nearestAnnotatable walks up past non annotatable nodes`() {
        val leaf = FakeNode("Leaf", Bounds(10, 10, 20, 20), annotatable = false)
        val middle = FakeNode("Middle", Bounds(5, 5, 30, 30), annotatable = false, children = listOf(leaf))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(middle))

        assertEquals(root, TargetSelection.nearestAnnotatable(leaf))
    }

    @Test
    fun `nearestAnnotatable returns the node itself when it qualifies`() {
        val leaf = FakeNode("Leaf", Bounds(10, 10, 20, 20))
        FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(leaf))

        assertEquals(leaf, TargetSelection.nearestAnnotatable(leaf))
    }

    @Test
    fun `stepUp broadens to the next annotatable ancestor`() {
        val leaf = FakeNode("Leaf", Bounds(10, 10, 20, 20))
        val middle = FakeNode("Middle", Bounds(5, 5, 30, 30), annotatable = false, children = listOf(leaf))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(middle))

        assertEquals(root, TargetSelection.stepUp(leaf))
    }

    @Test
    fun `stepUp returns null at the outermost annotatable node so the caller can wrap`() {
        val root = FakeNode("Root", Bounds(0, 0, 100, 100))

        assertNull(TargetSelection.stepUp(root))
    }

    @Test
    fun `pathOf joins the chain broadest first`() {
        val leaf = FakeNode("Icon", Bounds(10, 10, 20, 20))
        val middle = FakeNode("Button", Bounds(5, 5, 30, 30), children = listOf(leaf))
        FakeNode("Card", Bounds(0, 0, 100, 100), children = listOf(middle))

        assertEquals("Card>Button>Icon", TargetSelection.pathOf(leaf))
    }

    @Test
    fun `ordinalOf counts same type nodes top to bottom then left to right`() {
        val topLeft = FakeNode("Chip", Bounds(0, 0, 10, 10))
        val topRight = FakeNode("Chip", Bounds(20, 0, 30, 10))
        val bottom = FakeNode("Chip", Bounds(0, 40, 10, 50))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(bottom, topRight, topLeft))

        assertEquals(1 to 3, TargetSelection.ordinalOf(root, topLeft))
        assertEquals(2 to 3, TargetSelection.ordinalOf(root, topRight))
        assertEquals(3 to 3, TargetSelection.ordinalOf(root, bottom))
    }

    @Test
    fun `ordinalOf reports one of one for a lone node`() {
        val only = FakeNode("Chip", Bounds(0, 0, 10, 10))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(only))

        assertEquals(1 to 1, TargetSelection.ordinalOf(root, only))
    }
}
