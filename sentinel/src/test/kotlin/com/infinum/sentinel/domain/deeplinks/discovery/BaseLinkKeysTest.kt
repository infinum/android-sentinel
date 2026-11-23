package com.infinum.sentinel.domain.deeplinks.discovery

import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [BaseLinkKeys].
 */
internal class BaseLinkKeysTest {
    private fun key(
        host: HostSpec = HostSpec.Literal("example.com"),
        path: PathSpec = PathSpec.None,
        scheme: String = "https",
        port: Int? = null,
    ): String = BaseLinkKeys.declared(LinkSpec(scheme, host, port, path))

    @Test
    fun `declared joins literal host and literal path`() {
        assertEquals(
            "declared|https|lit:example.com||lit:/a",
            BaseLinkKeys.declared(LinkSpec("https", HostSpec.Literal("example.com"), null, PathSpec.Literal("/a"))),
        )
    }

    @Test
    fun `declared lowercases wildcard suffix`() {
        assertEquals("declared|https|wild:example.com||none", key(host = HostSpec.Wildcard("Example.com")))
    }

    @Test
    fun `declared writes any host`() {
        assertEquals("declared|https|any||none", key(host = HostSpec.AnyHost))
    }

    @Test
    fun `declared writes none host`() {
        assertEquals("declared|https|none||none", key(host = HostSpec.None))
    }

    @Test
    fun `declared writes prefix path`() {
        assertEquals("declared|https|lit:example.com||prefix:/o/", key(path = PathSpec.Prefix("/o/")))
    }

    @Test
    fun `declared writes pattern path`() {
        assertEquals("declared|https|lit:example.com||pattern:/p/.*", key(path = PathSpec.Pattern("/p/.*")))
    }

    @Test
    fun `declared writes advanced pattern path`() {
        assertEquals(
            "declared|https|lit:example.com||adv:/a/[0-9]+",
            key(path = PathSpec.AdvancedPattern("/a/[0-9]+")),
        )
    }

    @Test
    fun `declared writes suffix path`() {
        assertEquals("declared|https|lit:example.com||suffix:.pdf", key(path = PathSpec.Suffix(".pdf")))
    }

    @Test
    fun `declared writes port`() {
        assertEquals("declared|https|lit:example.com|8080|none", key(port = 8080))
    }

    @Test
    fun `declared lowercases scheme`() {
        assertEquals("declared|https|lit:example.com||none", key(scheme = "HTTPS"))
    }

    @Test
    fun `example prefixes raw text`() {
        assertEquals(
            "example|myapp://product/123?ref=push",
            BaseLinkKeys.example("myapp://product/123?ref=push"),
        )
    }
}
