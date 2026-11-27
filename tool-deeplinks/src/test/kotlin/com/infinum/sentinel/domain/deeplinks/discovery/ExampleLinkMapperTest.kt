package com.infinum.sentinel.domain.deeplinks.discovery

import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.ExampleLinkData
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkType
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [ExampleLinkMapper].
 */
internal class ExampleLinkMapperTest {
    @Test
    fun `map reads a custom scheme example`() {
        val uri = "myapp://product/123?ref=push&x=a%20b"

        val link = ExampleLinkMapper.map(listOf(ExampleLinkData(uri, "Product"))).single()

        assertEquals("example|myapp://product/123?ref=push&x=a%20b", link.key)
        assertEquals(LinkType.CUSTOM_SCHEME, link.type)
        assertEquals(LinkSpec("myapp", HostSpec.Literal("product"), null, PathSpec.Literal("/123")), link.spec)
        assertEquals(listOf(QueryParam("ref", "push"), QueryParam("x", "a b")), link.exampleUri?.params)
        assertEquals(uri, link.launchLink)
        assertEquals(uri, link.displayText)
        assertEquals(BaseLinkOrigin.Example("Product", uri), link.origin)
        assertEquals(setOf<ConfigurationWarning>(), link.warnings)
        assertFalse(link.isTemplate)
    }

    @Test
    fun `map marks https as web`() {
        assertEquals(
            LinkType.WEB,
            ExampleLinkMapper.map(listOf(ExampleLinkData("https://example.com/offers", null))).single().type,
        )
    }

    @Test
    fun `map gives no path for a link without one`() {
        val link = ExampleLinkMapper.map(listOf(ExampleLinkData("myapp://home", null))).single()

        assertEquals(PathSpec.None, link.spec?.path)
    }

    @Test
    fun `map keeps an unparseable example`() {
        val link = ExampleLinkMapper.map(listOf(ExampleLinkData("not a link", null))).single()

        assertNull(link.spec)
        assertNull(link.exampleUri)
        assertEquals("not a link", link.launchLink)
        assertEquals("not a link", link.displayText)
        assertEquals(LinkType.CUSTOM_SCHEME, link.type)
    }

    @Test
    fun `map merges the same uri and keeps the first label`() {
        val links =
            ExampleLinkMapper.map(
                listOf(ExampleLinkData("myapp://home", "First"), ExampleLinkData("myapp://home", "Second")),
            )

        assertEquals(1, links.size)
        assertEquals(BaseLinkOrigin.Example("First", "myapp://home"), links.single().origin)
    }
}
