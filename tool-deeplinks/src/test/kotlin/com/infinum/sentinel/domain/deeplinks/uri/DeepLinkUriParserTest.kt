package com.infinum.sentinel.domain.deeplinks.uri

import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [DeepLinkUriParser].
 */
internal class DeepLinkUriParserTest {
    @Test
    fun `parse splits a custom scheme link`() {
        val uri = DeepLinkUriParser.parse("myapp://product/123?promo=SUMMER%20SALE&ref=push")

        assertNotNull(uri)
        assertEquals("myapp", uri?.scheme)
        assertEquals(true, uri?.hasAuthority)
        assertEquals("product", uri?.host)
        assertNull(uri?.port)
        assertEquals("/123", uri?.path)
        assertEquals(listOf(QueryParam("promo", "SUMMER SALE"), QueryParam("ref", "push")), uri?.params)
        assertNull(uri?.fragment)
    }

    @Test
    fun `parse reads port, repeated keys and fragment`() {
        val uri = DeepLinkUriParser.parse("https://example.com:8080/a?x=1&x=2#frag")

        assertEquals("example.com", uri?.host)
        assertEquals(8080, uri?.port)
        assertEquals("/a", uri?.path)
        assertEquals(listOf(QueryParam("x", "1"), QueryParam("x", "2")), uri?.params)
        assertEquals("frag", uri?.fragment)
    }

    @Test
    fun `parse keeps empty value`() {
        assertEquals(listOf(QueryParam("debug", "")), DeepLinkUriParser.parse("myapp://h?debug=")?.params)
    }

    @Test
    fun `parse gives empty value for missing equals sign`() {
        assertEquals(listOf(QueryParam("flag", "")), DeepLinkUriParser.parse("myapp://h?flag")?.params)
    }

    @Test
    fun `parse reads plus as space in query`() {
        assertEquals(listOf(QueryParam("q", "a b")), DeepLinkUriParser.parse("myapp://h?q=a+b")?.params)
    }

    @Test
    fun `parse keeps plus in query keys`() {
        assertEquals(listOf(QueryParam("first+name", "a b")), DeepLinkUriParser.parse("myapp://h?first+name=a+b")?.params)
    }

    @Test
    fun `parse keeps invalid escape literal`() {
        assertEquals(listOf(QueryParam("q", "%zz")), DeepLinkUriParser.parse("myapp://h?q=%zz")?.params)
    }

    @Test
    fun `parse skips empty query segments`() {
        assertEquals(listOf(QueryParam("a", "1")), DeepLinkUriParser.parse("myapp://h?&&a=1")?.params)
    }

    @Test
    fun `parse returns null without scheme`() {
        assertNull(DeepLinkUriParser.parse("no-scheme/path"))
    }

    @Test
    fun `parse handles scheme only`() {
        val uri = DeepLinkUriParser.parse("myapp:")

        assertNotNull(uri)
        assertFalse(uri?.hasAuthority ?: true)
        assertNull(uri?.host)
        assertEquals("", uri?.path)
        assertTrue(uri?.params?.isEmpty() ?: false)
    }

    @Test
    fun `parse drops userinfo from host`() {
        assertEquals("example.com", DeepLinkUriParser.parse("https://user@example.com/a")?.host)
    }

    @Test
    fun `parse then build keeps an encoded link unchanged`() {
        val link = "myapp://product/123?promo=SUMMER%20SALE"

        val uri = DeepLinkUriParser.parse(link)

        assertEquals(link, uri?.let { DeepLinkUriBuilder.build(it) })
    }

    @Test
    fun `parse keeps user info apart from host and port`() {
        val uri = DeepLinkUriParser.parse("https://tester@example.com:8080/offers")

        assertEquals("tester", uri?.userInfo)
        assertEquals("example.com", uri?.host)
        assertEquals(8080, uri?.port)
        assertEquals("/offers", uri?.path)
    }

    @Test
    fun `parse gives no user info when the authority has no at sign`() {
        assertNull(DeepLinkUriParser.parse("https://example.com/offers")?.userInfo)
    }
}
