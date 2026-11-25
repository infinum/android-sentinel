package com.infinum.sentinel.domain.deeplinks.uri

import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [DeepLinkUriBuilder].
 */
internal class DeepLinkUriBuilderTest {
    private fun productUri(
        params: List<QueryParam> = emptyList(),
        fragment: String? = null,
    ) = DeepLinkUri(
        scheme = "myapp",
        hasAuthority = true,
        host = "product",
        port = null,
        path = "/123",
        params = params,
        fragment = fragment,
    )

    @Test
    fun `build encodes query keys and values`() {
        val uri = productUri(listOf(QueryParam("promo", "SUMMER SALE"), QueryParam("ref", "push&email")))

        assertEquals("myapp://product/123?promo=SUMMER%20SALE&ref=push%26email", DeepLinkUriBuilder.build(uri))
    }

    @Test
    fun `build keeps equals sign for empty value`() {
        val uri = productUri(listOf(QueryParam("debug", "")))

        assertEquals("myapp://product/123?debug=", DeepLinkUriBuilder.build(uri))
    }

    @Test
    fun `build preserves order and duplicates`() {
        val uri = productUri(listOf(QueryParam("tag", "a"), QueryParam("tag", "b")))

        assertEquals("myapp://product/123?tag=a&tag=b", DeepLinkUriBuilder.build(uri))
    }

    @Test
    fun `build includes port`() {
        val uri = DeepLinkUri("https", true, "example.com", 8080, "/a", emptyList(), null)

        assertEquals("https://example.com:8080/a", DeepLinkUriBuilder.build(uri))
    }

    @Test
    fun `build omits question mark without params`() {
        assertEquals("myapp://product/123", DeepLinkUriBuilder.build(productUri()))
    }

    @Test
    fun `build appends fragment`() {
        assertEquals("myapp://product/123#top", DeepLinkUriBuilder.build(productUri(fragment = "top")))
    }

    @Test
    fun `build omits authority when absent`() {
        val uri = DeepLinkUri("mailto", false, null, null, "a@b.c", emptyList(), null)

        assertEquals("mailto:a@b.c", DeepLinkUriBuilder.build(uri))
    }
}
