package com.infinum.sentinel.domain.deeplinks.discovery

import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.DeclaredData
import com.infinum.sentinel.domain.deeplinks.models.DeclaredFilter
import com.infinum.sentinel.domain.deeplinks.models.DeclaredGroup
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkType
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [BaseLinkExpander].
 */
internal class BaseLinkExpanderTest {
    private val default = "android.intent.category.DEFAULT"
    private val browsable = "android.intent.category.BROWSABLE"

    private fun filter(
        vararg data: DeclaredData,
        component: String = "com.example.A",
        title: String = "A",
        categories: List<String> = listOf(default, browsable),
        groups: List<DeclaredGroup> = listOf(),
    ): DeclaredFilter = DeclaredFilter(component, title, categories, data.toList(), groups)

    private fun links(
        filter: DeclaredFilter,
        sdkInt: Int = 37,
    ): List<BaseLink> = BaseLinkExpander.expand(listOf(filter), sdkInt).flatMap { it.links }

    private fun texts(
        filter: DeclaredFilter,
        sdkInt: Int = 37,
    ): List<String> = links(filter, sdkInt).map { it.displayText }

    private val site = DeclaredData(scheme = "https", host = "example.com")

    @Test
    fun `expand keeps schemes that differ only in case as separate links`() {
        val result = texts(filter(DeclaredData(scheme = "myapp"), DeclaredData(scheme = "MyApp"), DeclaredData(host = "h")))

        assertEquals(listOf("myapp://h", "MyApp://h"), result)
    }

    @Test
    fun `expand makes one link per scheme`() {
        val result = links(filter(DeclaredData(scheme = "http"), DeclaredData(scheme = "https"), DeclaredData(host = "example.com")))

        assertEquals(listOf("http://example.com", "https://example.com"), result.map { it.displayText })
        assertEquals(listOf(LinkType.WEB, LinkType.WEB), result.map { it.type })
        assertEquals(listOf(false, false), result.map { it.isTemplate })
        assertEquals(listOf(PathSpec.None, PathSpec.None), result.map { it.spec?.path })
        assertEquals(listOf("http://example.com", "https://example.com"), result.map { it.launchLink })
    }

    @Test
    fun `expand combines schemes authorities and paths in order`() {
        val result =
            texts(
                filter(
                    DeclaredData(scheme = "https", host = "a.com"),
                    DeclaredData(scheme = "http", host = "b.com"),
                    DeclaredData(path = "/x"),
                    DeclaredData(pathPrefix = "/y/"),
                ),
            )

        assertEquals(
            listOf(
                "https://a.com/x",
                "https://a.com/y/*",
                "https://b.com/x",
                "https://b.com/y/*",
                "http://a.com/x",
                "http://a.com/y/*",
                "http://b.com/x",
                "http://b.com/y/*",
            ),
            result,
        )
    }

    @Test
    fun `expand marks wildcard host with prefix path as template`() {
        val link = links(filter(DeclaredData(scheme = "https", host = "*.example.com", pathPrefix = "/orders/"))).single()

        assertEquals("https://*.example.com/orders/*", link.displayText)
        assertTrue(link.isTemplate)
        assertNull(link.launchLink)
        assertEquals("declared|https|wild:.example.com||prefix:/orders/", link.key)
        assertEquals(HostSpec.Wildcard(".example.com"), link.spec?.host)
        assertEquals(PathSpec.Prefix("/orders/"), link.spec?.path)
    }

    @Test
    fun `expand marks a leading star without a dot as a wildcard template`() {
        val link = links(filter(DeclaredData(scheme = "https", host = "*example.com"))).single()

        assertEquals("https://*example.com", link.displayText)
        assertTrue(link.isTemplate)
        assertNull(link.launchLink)
        assertEquals("declared|https|wild:example.com||none", link.key)
        assertEquals(HostSpec.Wildcard("example.com"), link.spec?.host)
    }

    @Test
    fun `expand marks any host as template`() {
        val link = links(filter(DeclaredData(scheme = "myapp", host = "*"))).single()

        assertEquals("myapp://*", link.displayText)
        assertTrue(link.isTemplate)
        assertEquals(HostSpec.AnyHost, link.spec?.host)
    }

    @Test
    fun `expand gives a bare scheme link with optional parts`() {
        val link = links(filter(DeclaredData(scheme = "myapp"))).single()

        assertEquals("myapp://", link.displayText)
        assertFalse(link.isTemplate)
        assertEquals("myapp://", link.launchLink)
        assertEquals(HostSpec.None, link.spec?.host)
        assertEquals(PathSpec.None, link.spec?.path)
    }

    @Test
    fun `expand ignores paths without a host`() {
        val result = links(filter(DeclaredData(scheme = "myapp"), DeclaredData(path = "/x")))

        assertEquals(listOf("myapp://"), result.map { it.displayText })
    }

    @Test
    fun `expand ignores a port without a host`() {
        val link = links(filter(DeclaredData(scheme = "https", host = "example.com", port = "8080"), DeclaredData(port = "9090"))).single()

        assertEquals("https://example.com:8080", link.displayText)
        assertEquals("declared|https|lit:example.com|8080|none", link.key)
        assertEquals("https://example.com:8080", link.launchLink)
    }

    @Test
    fun `expand encodes the launch link but not the display text`() {
        val link = links(filter(site, DeclaredData(path = "/a b"))).single()

        assertEquals("https://example.com/a b", link.displayText)
        assertEquals("https://example.com/a%20b", link.launchLink)
    }

    @Test
    fun `expand adds a leading slash to a declared path in the launch link`() {
        val link = links(filter(site, DeclaredData(path = "offers"))).single()

        assertEquals("https://example.com/offers", link.launchLink)
    }

    @Test
    fun `expand supports path suffix from Android 12`() {
        val data = filter(site, DeclaredData(pathSuffix = ".pdf"))

        assertEquals(listOf("https://example.com"), texts(data, 30))
        val link = links(data, 31).single()
        assertEquals("https://example.com/*.pdf", link.displayText)
        assertTrue(link.isTemplate)
        assertEquals(PathSpec.Suffix(".pdf"), link.spec?.path)
    }

    @Test
    fun `expand supports advanced pattern from Android 12`() {
        val data = filter(site, DeclaredData(pathAdvancedPattern = "/a/[0-9]+"))

        assertEquals(listOf("https://example.com"), texts(data, 30))
        assertEquals(listOf("https://example.com/a/[0-9]+"), texts(data, 31))
    }

    @Test
    fun `expand keeps the plain path when the suffix is unsupported`() {
        assertEquals(listOf("https://example.com/x"), texts(filter(site, DeclaredData(path = "/x", pathSuffix = ".pdf")), 30))
    }

    @Test
    fun `expand adds allowed group paths from Android 15`() {
        val allowed = filter(site, groups = listOf(DeclaredGroup(true, listOf(DeclaredData(path = "/group")))))

        assertEquals(listOf("https://example.com/group"), texts(allowed, 35))
        assertEquals(listOf("https://example.com"), texts(allowed, 34))
    }

    @Test
    fun `expand ignores a disallowed group`() {
        val denied = filter(site, groups = listOf(DeclaredGroup(false, listOf(DeclaredData(path = "/group")))))

        assertEquals(listOf("https://example.com"), texts(denied, 35))
    }

    @Test
    fun `expand ignores a group without path fields`() {
        val empty = filter(site, groups = listOf(DeclaredGroup(true, listOf(DeclaredData()))))

        assertEquals(listOf("https://example.com"), texts(empty, 35))
    }

    @Test
    fun `expand merges the same link of one component and keeps shared warnings`() {
        val screens =
            BaseLinkExpander.expand(
                listOf(filter(site, categories = listOf(browsable)), filter(site)),
                37,
            )

        assertEquals(1, screens.size)
        assertEquals(1, screens.single().links.size)
        assertEquals(setOf<ConfigurationWarning>(), screens.single().links.single().warnings)
    }

    @Test
    fun `expand merges declarations split between default and browsable into a missing browsable warning`() {
        val screens =
            BaseLinkExpander.expand(
                listOf(filter(site, categories = listOf(browsable)), filter(site, categories = listOf(default))),
                37,
            )

        assertEquals(1, screens.single().links.size)
        assertEquals(setOf(ConfigurationWarning.MissingBrowsable), screens.single().links.single().warnings)
    }

    @Test
    fun `expand puts a slash between the host and a path that lacks one`() {
        val data = filter(site, DeclaredData(pathPattern = ".*"))

        assertEquals(listOf("https://example.com/.*"), texts(data))
    }

    @Test
    fun `expand warns about a missing default category`() {
        assertEquals(setOf(ConfigurationWarning.MissingDefault), links(filter(site, categories = listOf(browsable))).single().warnings)
    }

    @Test
    fun `expand warns about a missing browsable category`() {
        assertEquals(setOf(ConfigurationWarning.MissingBrowsable), links(filter(site, categories = listOf(default))).single().warnings)
    }

    @Test
    fun `expand keeps screen order and titles`() {
        val screens =
            BaseLinkExpander.expand(
                listOf(
                    filter(site, component = "com.example.A", title = "Alpha"),
                    filter(site, component = "com.example.B", title = "Beta"),
                ),
                37,
            )

        assertEquals(listOf("com.example.A", "com.example.B"), screens.map { it.componentName })
        assertEquals(listOf("Alpha", "Beta"), screens.map { it.title })
    }

    @Test
    fun `expand keeps host as declared but compares it case-insensitively in the key`() {
        val link = links(filter(DeclaredData(scheme = "https", host = "Example.COM"))).single()

        assertEquals("https://Example.COM", link.displayText)
        assertEquals("declared|https|lit:example.com||none", link.key)
    }
}
