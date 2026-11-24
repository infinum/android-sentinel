package com.infinum.sentinel.ui.deeplinks.list

import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.Variant
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksNotice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [DeepLinksItemKeys].
 */
internal class DeepLinksItemKeysTest {
    @Test
    fun `lazyKeys gives distinct keys when two screens declare the same link key`() {
        val keys =
            DeepLinksItemKeys.lazyKeys(
                listOf(baseRow("k", "com.example.A"), baseRow("k", "com.example.B")),
            )

        assertEquals(2, keys.size)
        assertNotEquals(keys[0], keys[1])
    }

    @Test
    fun `lazyKeys suffixes a repeated item`() {
        val row = variantRow("v1", baseLink = null)

        assertEquals(listOf("variant:v1", "variant:v1#1"), DeepLinksItemKeys.lazyKeys(listOf(row, row)))
    }

    @Test
    fun `lazyKeys follows the item identities`() {
        val items =
            listOf(
                DeepLinksItem.Notice(DeepLinksNotice.LINK_HANDLING_OFF),
                DeepLinksItem.ScreenHeader("com.example.Main", "Main"),
                variantRow("v2", baseLink = null),
            )

        assertEquals(
            listOf("notice:LINK_HANDLING_OFF", "screen:com.example.Main", "variant:v2"),
            DeepLinksItemKeys.lazyKeys(items),
        )
    }

    @Test
    fun `hasDividerAbove is true for a base row after a variant row`() {
        val items = listOf(baseRow("a", "com.example.A"), variantRow("v1", baseLink(key = "a")), baseRow("b", "com.example.A"))

        assertTrue(DeepLinksItemKeys.hasDividerAbove(items, 2))
    }

    @Test
    fun `hasDividerAbove is false for a variant under its base row`() {
        val items = listOf(baseRow("a", "com.example.A"), variantRow("v1", baseLink(key = "a")))

        assertFalse(DeepLinksItemKeys.hasDividerAbove(items, 1))
    }

    @Test
    fun `hasDividerAbove is false for the first row after a header`() {
        val items = listOf(DeepLinksItem.ScreenHeader("com.example.Main", "Main"), baseRow("a", "com.example.A"))

        assertFalse(DeepLinksItemKeys.hasDividerAbove(items, 1))
    }

    @Test
    fun `hasDividerAbove is true for an orphan variant after an orphan variant`() {
        val items = listOf(variantRow("v1", baseLink = null), variantRow("v2", baseLink = null))

        assertTrue(DeepLinksItemKeys.hasDividerAbove(items, 1))
    }

    @Test
    fun `hasDividerAbove is false at index 0 and out of range`() {
        val items = listOf(baseRow("a", "com.example.A"), baseRow("b", "com.example.A"))

        assertFalse(DeepLinksItemKeys.hasDividerAbove(items, 0))
        assertFalse(DeepLinksItemKeys.hasDividerAbove(items, 2))
        assertFalse(DeepLinksItemKeys.hasDividerAbove(items, -1))
    }

    private fun baseLink(
        key: String = "k",
        component: String = "com.example.A",
    ) = BaseLink(key = key, origin = BaseLinkOrigin.Declared(component), spec = null, exampleUri = null)

    private fun baseRow(
        key: String,
        component: String,
    ) = DeepLinksItem.BaseLinkRow(
        baseLink = baseLink(key = key, component = component),
        variantCount = 0,
        title = null,
        link = "scheme://$key",
        launchLink = null,
        status = null,
    )

    private fun variant(id: String) =
        Variant(
            id = id,
            name = null,
            baseKey = "k",
            componentName = null,
            uri = DeepLinkUri("scheme", true, "host", null, "/path", listOf(), null),
        )

    private fun variantRow(
        id: String,
        baseLink: BaseLink?,
    ) = DeepLinksItem.VariantRow(
        variant = variant(id),
        baseLink = baseLink,
        isOrphan = false,
        title = null,
        link = "scheme://host/path",
        status = null,
    )
}
