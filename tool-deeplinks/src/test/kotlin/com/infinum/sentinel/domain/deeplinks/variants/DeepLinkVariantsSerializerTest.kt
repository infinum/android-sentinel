package com.infinum.sentinel.domain.deeplinks.variants

import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import com.infinum.sentinel.domain.deeplinks.models.Variant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [DeepLinkVariantsSerializer].
 */
internal class DeepLinkVariantsSerializerTest {
    private val promo =
        Variant(
            id = "v1",
            name = "Promo",
            baseKey = "example|myapp://p",
            componentName = null,
            uri = DeepLinkUri(
                scheme = "myapp",
                hasAuthority = true,
                host = "p",
                port = null,
                path = "",
                params = listOf(QueryParam("promo", "SUMMER")),
                fragment = null,
            ),
        )

    private val promoJson =
        """{"version":1,"variants":[{"id":"v1","name":"Promo","baseKey":"example|myapp://p",""" +
            """"scheme":"myapp","authority":true,"host":"p","path":"","params":[{"k":"promo","v":"SUMMER"}]}]}"""

    @Test
    fun `encode writes the exact document`() {
        assertEquals(promoJson, DeepLinkVariantsSerializer.encode(listOf(promo)))
    }

    @Test
    fun `decode reads the exact document`() {
        assertEquals(listOf(promo), DeepLinkVariantsSerializer.decode(promoJson))
    }

    @Test
    fun `variant with every field set survives a round trip`() {
        val full =
            Variant(
                id = "v2",
                name = null,
                baseKey = "declared|https|lit:example.com|8080|prefix:/a",
                componentName = "com.example.app.DeepLinkActivity",
                uri = DeepLinkUri(
                    scheme = "https",
                    hasAuthority = true,
                    host = "example.com",
                    port = 8080,
                    path = "/a/b",
                    params = listOf(QueryParam("a", "1"), QueryParam("b", "two words")),
                    fragment = "top",
                ),
            )
        val expected = listOf(full)

        assertEquals(expected, DeepLinkVariantsSerializer.decode(DeepLinkVariantsSerializer.encode(expected)))
    }

    @Test
    fun `decode reads a document with every field set`() {
        val json =
            """{"version":1,"variants":[{"id":"v2","name":"Full","baseKey":"declared|https|lit:example.com|8080|prefix:/a",""" +
                """"component":"com.example.app.DeepLinkActivity","scheme":"https","authority":true,"host":"example.com",""" +
                """"port":8080,"path":"/a/b","fragment":"top","params":[{"k":"a","v":"1"},{"k":"b","v":"two words"}]}]}"""
        val expected =
            Variant(
                id = "v2",
                name = "Full",
                baseKey = "declared|https|lit:example.com|8080|prefix:/a",
                componentName = "com.example.app.DeepLinkActivity",
                uri = DeepLinkUri(
                    scheme = "https",
                    hasAuthority = true,
                    host = "example.com",
                    port = 8080,
                    path = "/a/b",
                    params = listOf(QueryParam("a", "1"), QueryParam("b", "two words")),
                    fragment = "top",
                ),
            )

        assertEquals(listOf(expected), DeepLinkVariantsSerializer.decode(json))
    }

    @Test
    fun `decode returns null for a document from a newer version`() {
        assertNull(DeepLinkVariantsSerializer.decode("""{"version":2,"variants":[]}"""))
    }

    @Test
    fun `decode ignores unknown keys`() {
        val json =
            """{"version":1,"extra":1,"variants":[{"id":"v1","extra":1,"name":"Promo","baseKey":"example|myapp://p",""" +
                """"scheme":"myapp","authority":true,"host":"p","path":"","params":[{"k":"promo","v":"SUMMER"}]}]}"""

        assertEquals(listOf(promo), DeepLinkVariantsSerializer.decode(json))
    }

    @Test
    fun `decode returns null for malformed json`() {
        assertNull(DeepLinkVariantsSerializer.decode("{not json"))
    }

    @Test
    fun `decode returns null for wrong types`() {
        assertNull(DeepLinkVariantsSerializer.decode("""{"version":1,"variants":"x"}"""))
    }

    @Test
    fun `decode returns null when a variant misses its id`() {
        val json =
            """{"version":1,"variants":[{"name":"Promo","baseKey":"k","scheme":"myapp","authority":true,"path":""}]}"""

        assertNull(DeepLinkVariantsSerializer.decode(json))
    }

    @Test
    fun `encode of an empty list writes an empty document`() {
        assertEquals("""{"version":1,"variants":[]}""", DeepLinkVariantsSerializer.encode(emptyList()))
    }
}
