package com.infinum.sentinel.domain.deeplinks.uri

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [DeepLinkUriEncoder].
 */
internal class DeepLinkUriEncoderTest {
    @Test
    fun `encodeQueryComponent encodes space as percent 20`() {
        assertEquals("summer%20sale", DeepLinkUriEncoder.encodeQueryComponent("summer sale"))
    }

    @Test
    fun `encodeQueryComponent encodes reserved characters`() {
        assertEquals("a%26b", DeepLinkUriEncoder.encodeQueryComponent("a&b"))
        assertEquals("x%3Dy", DeepLinkUriEncoder.encodeQueryComponent("x=y"))
        assertEquals("%231", DeepLinkUriEncoder.encodeQueryComponent("#1"))
        assertEquals("why%3F", DeepLinkUriEncoder.encodeQueryComponent("why?"))
    }

    @Test
    fun `encodeQueryComponent encodes plus and percent`() {
        assertEquals("1%2B1", DeepLinkUriEncoder.encodeQueryComponent("1+1"))
        assertEquals("100%25", DeepLinkUriEncoder.encodeQueryComponent("100%"))
    }

    @Test
    fun `encodeQueryComponent encodes non ASCII as UTF-8 bytes`() {
        assertEquals("%C4%8D", DeepLinkUriEncoder.encodeQueryComponent("č"))
        assertEquals("%E6%97%A5%E6%9C%AC", DeepLinkUriEncoder.encodeQueryComponent("日本"))
        assertEquals("%F0%9F%98%80", DeepLinkUriEncoder.encodeQueryComponent("😀"))
    }

    @Test
    fun `encodeQueryComponent leaves unreserved characters unchanged`() {
        assertEquals("AZaz09-._~", DeepLinkUriEncoder.encodeQueryComponent("AZaz09-._~"))
    }

    @Test
    fun `encodePath encodes space`() {
        assertEquals("/orders/12%2034", DeepLinkUriEncoder.encodePath("/orders/12 34"))
    }

    @Test
    fun `encodePath encodes hash and question mark`() {
        assertEquals("/a%23b%3Fc", DeepLinkUriEncoder.encodePath("/a#b?c"))
    }

    @Test
    fun `encodePath leaves pchar characters unchanged`() {
        assertEquals("/a:b@c;d", DeepLinkUriEncoder.encodePath("/a:b@c;d"))
    }

    @Test
    fun `decodeComponent decodes percent escapes`() {
        assertEquals("summer sale", DeepLinkUriEncoder.decodeComponent("summer%20sale", plusAsSpace = false))
    }

    @Test
    fun `decodeComponent decodes multi byte characters`() {
        assertEquals("č", DeepLinkUriEncoder.decodeComponent("%C4%8D", plusAsSpace = false))
    }

    @Test
    fun `decodeComponent reads plus as space only when requested`() {
        assertEquals("a b", DeepLinkUriEncoder.decodeComponent("a+b", plusAsSpace = true))
        assertEquals("a+b", DeepLinkUriEncoder.decodeComponent("a+b", plusAsSpace = false))
    }

    @Test
    fun `decodeComponent keeps invalid sequences literal`() {
        assertEquals("%zz", DeepLinkUriEncoder.decodeComponent("%zz", plusAsSpace = false))
        assertEquals("abc%4", DeepLinkUriEncoder.decodeComponent("abc%4", plusAsSpace = false))
    }
}
