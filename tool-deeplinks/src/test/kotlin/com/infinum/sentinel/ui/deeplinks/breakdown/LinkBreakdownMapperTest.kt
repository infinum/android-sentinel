package com.infinum.sentinel.ui.deeplinks.breakdown

import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [LinkBreakdownMapper].
 */
internal class LinkBreakdownMapperTest {
    @Test
    fun `map splits a custom scheme link into all parts`() {
        val link = "myapp://product/123?promo=SUMMER%20SALE&tag=a&tag=b#top"

        assertEquals(
            LinkBreakdown.Parsed(
                fullLink = link,
                scheme = "myapp",
                host = "product",
                port = null,
                path = "/123",
                params = listOf(QueryParam("promo", "SUMMER SALE"), QueryParam("tag", "a"), QueryParam("tag", "b")),
                fragment = "top",
            ),
            LinkBreakdownMapper.map(link),
        )
    }

    @Test
    fun `map reads the port and decodes the path`() {
        val link = "https://example.com:8080/a%20b"

        assertEquals(
            LinkBreakdown.Parsed(
                fullLink = link,
                scheme = "https",
                host = "example.com",
                port = 8080,
                path = "/a b",
                params = emptyList(),
                fragment = null,
            ),
            LinkBreakdownMapper.map(link),
        )
    }

    @Test
    fun `map reads a plus in a query value as a space`() {
        val result = LinkBreakdownMapper.map("myapp://search?q=a+b") as LinkBreakdown.Parsed

        assertEquals(listOf(QueryParam("q", "a b")), result.params)
    }

    @Test
    fun `map keeps a parameter with an empty value`() {
        val result = LinkBreakdownMapper.map("myapp://h?debug=") as LinkBreakdown.Parsed

        assertEquals(listOf(QueryParam("debug", "")), result.params)
    }

    @Test
    fun `map keeps the declared parts of a template link`() {
        val result = LinkBreakdownMapper.map("https://*.example.com/orders/*") as LinkBreakdown.Parsed

        assertEquals("*.example.com", result.host)
        assertEquals("/orders/*", result.path)
    }

    @Test
    fun `map returns unparseable for text without a scheme`() {
        assertEquals(LinkBreakdown.Unparseable("not a link"), LinkBreakdownMapper.map("not a link"))
    }
}
