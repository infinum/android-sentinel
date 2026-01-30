package com.infinum.sentinel.extensions

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android instrumented tests for Bundle extension functions.
 * Tests bundle size calculations and tree generation.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
internal class BundleExtensionsTest {
    @Test
    fun sizeAsParcelable_emptyBundle_returnsNonZeroSize() {
        val bundle = Bundle()

        val size = bundle.sizeAsParcelable

        assertTrue("Empty bundle should have non-zero size", size > 0)
    }

    @Test
    fun sizeAsParcelable_bundleWithString_returnsGreaterSize() {
        val emptyBundle = Bundle()
        val bundleWithData =
            Bundle().apply {
                putString("key", "value")
            }

        val emptySize = emptyBundle.sizeAsParcelable
        val dataSize = bundleWithData.sizeAsParcelable

        assertTrue("Bundle with data should be larger", dataSize > emptySize)
    }

    @Test
    fun sizeAsParcelable_bundleWithMultipleValues_returnsAppropriateSize() {
        val bundle =
            Bundle().apply {
                putString("string", "test")
                putInt("int", 42)
                putBoolean("boolean", true)
                putLong("long", 1000L)
            }

        val size = bundle.sizeAsParcelable

        assertTrue("Bundle with multiple values should have size > 0", size > 0)
    }

    @Test
    fun sizeTree_emptyBundle_returnsValidTree() {
        val bundle = Bundle()

        val tree = bundle.sizeTree()

        assertNotNull("Tree should not be null", tree)
        assertTrue("Tree should have non-zero size", tree.size > 0)
        assertTrue("Tree subTrees should be empty", tree.subTrees.isEmpty())
    }

    @Test
    fun sizeTree_bundleWithNestedBundle_returnsTreeWithChildren() {
        val nestedBundle =
            Bundle().apply {
                putString("nested_key", "nested_value")
            }
        val bundle =
            Bundle().apply {
                putBundle("nested", nestedBundle)
            }

        val tree = bundle.sizeTree()

        assertNotNull("Tree should not be null", tree)
        assertEquals("Tree should have one child", 1, tree.subTrees.size)
        assertEquals("Child id should match", "nested", tree.subTrees[0].id)
        assertTrue("Child should have non-zero size", tree.subTrees[0].size > 0)
    }

    @Test
    fun sizeTree_bundleWithMultipleNestedBundles_returnsTreeWithMultipleChildren() {
        val nested1 = Bundle().apply { putString("key1", "value1") }
        val nested2 = Bundle().apply { putString("key2", "value2") }
        val bundle =
            Bundle().apply {
                putBundle("bundle1", nested1)
                putBundle("bundle2", nested2)
            }

        val tree = bundle.sizeTree()

        assertEquals("Tree should have two children", 2, tree.subTrees.size)
    }

    @Test
    fun sizeTree_bundleWithDeeplyNestedBundles_returnsTreeWithDepth() {
        val deeplyNested =
            Bundle().apply {
                putString("deep_key", "deep_value")
            }
        val middleNested =
            Bundle().apply {
                putBundle("deep", deeplyNested)
            }
        val topBundle =
            Bundle().apply {
                putBundle("middle", middleNested)
            }

        val tree = topBundle.sizeTree()

        assertNotNull("Tree should not be null", tree)
        assertEquals("Tree should have one child", 1, tree.subTrees.size)

        val middleChild = tree.subTrees[0]
        assertEquals("Middle child should have one child", 1, middleChild.subTrees.size)
    }

    @Test
    fun sizeTree_bundleWithPrimitiveValues_returnsTreeWithNoChildren() {
        val bundle =
            Bundle().apply {
                putString("string", "test")
                putInt("int", 42)
                putBoolean("boolean", true)
            }

        val tree = bundle.sizeTree()

        assertNotNull("Tree should not be null", tree)
        assertTrue("Tree subTrees should be empty for non-bundle values", tree.subTrees.isEmpty())
    }

    @Test
    fun sizeTree_bundleWithMixedContent_returnsTreeWithOnlyBundleChildren() {
        val nestedBundle =
            Bundle().apply {
                putString("nested", "value")
            }
        val bundle =
            Bundle().apply {
                putString("string", "test")
                putInt("int", 42)
                putBundle("bundle", nestedBundle)
                putBoolean("boolean", true)
            }

        val tree = bundle.sizeTree()

        assertEquals("Tree should have only one child (the bundle)", 1, tree.subTrees.size)
        assertEquals("Child id should be 'bundle'", "bundle", tree.subTrees[0].id)
    }

    @Test
    fun sizeTree_differentBundleInstances_generateDifferentIds() {
        val bundle1 = Bundle()
        val bundle2 = Bundle()

        val tree1 = bundle1.sizeTree()
        val tree2 = bundle2.sizeTree()

        assertTrue(
            "IDs should be different (based on identity hash)",
            tree1.id != tree2.id,
        )
    }
}
