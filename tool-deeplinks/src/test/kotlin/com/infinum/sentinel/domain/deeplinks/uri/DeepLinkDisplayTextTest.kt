package com.infinum.sentinel.domain.deeplinks.uri

import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [DeepLinkDisplayText].
 */
internal class DeepLinkDisplayTextTest {
    private fun link(
        path: String = "",
        vararg params: Pair<String, String>,
    ) = DeepLinkUri("myapp", true, "search", null, path, params.map { QueryParam(it.first, it.second) }, null)

    @Test
    fun `spaces in a value read as spaces and an ampersand stays encoded`() {
        assertEquals("myapp://search?q=summer sale %26 more", DeepLinkDisplayText.format(link("", "q" to "summer sale & more")))
    }

    @Test
    fun `a plus in a query stays encoded`() {
        assertEquals("myapp://search?a%2Bb=c%2Bd", DeepLinkDisplayText.format(link("", "a+b" to "c+d")))
    }

    @Test
    fun `equals and hash in a value stay encoded`() {
        assertEquals("myapp://search?q=a%3Db%23c", DeepLinkDisplayText.format(link("", "q" to "a=b#c")))
    }

    @Test
    fun `non-ASCII letters read as they are`() {
        assertEquals("myapp://search/café?q=žuto", DeepLinkDisplayText.format(link("/caf%C3%A9", "q" to "žuto")))
    }

    @Test
    fun `path escapes are decoded except a slash question mark hash and percent`() {
        assertEquals("myapp://search/a b/c%2Fd%3Fe%23f%25g", DeepLinkDisplayText.format(link("/a%20b/c%2Fd%3Fe%23f%25g")))
    }

    @Test
    fun `an escape that is not valid UTF-8 stays as it is`() {
        assertEquals("myapp://search/p%FF", DeepLinkDisplayText.format(link("/p%FF")))
        assertEquals("myapp://search/p%C3", DeepLinkDisplayText.format(link("/p%C3")))
    }

    @Test
    fun `a bidi override and a zero-width space stay encoded`() {
        assertEquals("myapp://search?q=a%E2%80%AEb%E2%80%8Bc", DeepLinkDisplayText.format(link("", "q" to "a\u202Eb\u200Bc")))
    }

    @Test
    fun `control characters and a no-break space stay encoded`() {
        assertEquals("myapp://search?q=a%0Ab%C2%A0c", DeepLinkDisplayText.format(link("", "q" to "a\nb\u00A0c")))
    }

    @Test
    fun `a space at the start or end of a value or path stays encoded`() {
        assertEquals("myapp://search/offer%20?q=%20a b%20", DeepLinkDisplayText.format(link("/offer%20", "q" to " a b ")))
    }

    @Test
    fun `raw path characters stay as they are`() {
        assertEquals("myapp://search/a/b+c%zz", DeepLinkDisplayText.format(link("/a/b+c%zz")))
        assertEquals("myapp://search/a%\uFF10\uFF10", DeepLinkDisplayText.format(link("/a%\uFF10\uFF10")))
    }

    @Test
    fun `characters outside the basic plane read as they are`() {
        assertEquals("myapp://search/\uD83D\uDE00?q=\uD83D\uDE00", DeepLinkDisplayText.format(link("/%F0%9F%98%80", "q" to "\uD83D\uDE00")))
    }

    @Test
    fun `lowercase escapes are decoded and an invalid run stays whole`() {
        assertEquals("myapp://search/caf\u00E9", DeepLinkDisplayText.format(link("/caf%c3%a9")))
        assertEquals("myapp://search/x%C3%A9%FF", DeepLinkDisplayText.format(link("/x%C3%A9%FF")))
    }

    @Test
    fun `a raw hidden character in the path is encoded`() {
        assertEquals("myapp://search/a%E2%80%AEb", DeepLinkDisplayText.format(link("/a\u202Eb")))
    }

    @Test
    fun `every space at either end stays encoded and inner spaces read as spaces`() {
        assertEquals("myapp://search/offers%20%20", DeepLinkDisplayText.format(link("/offers%20%20")))
        assertEquals("myapp://search/offer%20%20", DeepLinkDisplayText.format(link("/offer%20 ")))
        assertEquals("myapp://search?q=%20%20a  b%20%20", DeepLinkDisplayText.format(link("", "q" to "  a  b  ")))
    }

    @Test
    fun `a value that is a single space stays encoded`() {
        assertEquals("myapp://search?q=%20", DeepLinkDisplayText.format(link("", "q" to " ")))
    }

    @Test
    fun `blank-looking letters stay encoded`() {
        assertEquals("myapp://search?q=a%E3%85%A4b%E2%A0%80c", DeepLinkDisplayText.format(link("", "q" to "a\u3164b\u2800c")))
    }

    @Test
    fun `user info port and fragment are shown as stored`() {
        val uri = DeepLinkUri("https", true, "example.com", 8080, "/p", listOf(), "top%20x", userInfo = "tester")

        assertEquals("https://tester@example.com:8080/p#top%20x", DeepLinkDisplayText.format(uri))
    }
}
