package com.infinum.sentinel.domain.deeplinks.variants

import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import com.infinum.sentinel.domain.deeplinks.models.Variant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

/**
 * Unit tests for [DeepLinkVariantsRepository].
 */
internal class DeepLinkVariantsRepositoryTest {
    private val fake = FakeDeepLinksStore()

    private val a =
        Variant(
            id = "a-id",
            name = null,
            baseKey = "example|myapp://a",
            componentName = null,
            uri = DeepLinkUri("myapp", true, "a", null, "", emptyList(), null),
        )
    private val b =
        Variant(
            id = "b-id",
            name = "Product 123",
            baseKey = "example|myapp://product/123",
            componentName = "com.example.app.ProductActivity",
            uri = DeepLinkUri("myapp", true, "product", null, "/123", listOf(QueryParam("ref", "push")), null),
        )
    private val c =
        Variant(
            id = "c-id",
            name = "Web",
            baseKey = "example|https://example.com/c",
            componentName = null,
            uri = DeepLinkUri("https", true, "example.com", 8080, "/c", emptyList(), "top"),
        )

    private fun repository() =
        DeepLinkVariantsRepository(
            store = fake,
            idFactory = { "new-id" },
            ioDispatcher = Dispatchers.Unconfined,
        )

    private fun DeepLinkVariantsRepository.list(): List<Variant> = runBlocking { observe().first() }

    private fun repositoryWith(vararg variants: Variant): DeepLinkVariantsRepository {
        fake.variantsJson = DeepLinkVariantsSerializer.encode(variants.toList())
        return repository()
    }

    @Test
    fun `empty store yields an empty list`() {
        assertEquals(emptyList<Variant>(), repository().list())
    }

    @Test
    fun `stored variants are loaded`() {
        fake.variantsJson =
            """{"version":1,"variants":[{"id":"a-id","baseKey":"example|myapp://a","scheme":"myapp","authority":true,""" +
                """"host":"a","path":""}]}"""

        assertEquals(listOf(a), repository().list())
    }

    @Test
    fun `upsert appends new variants and persists them`() =
        runBlocking {
            val repository = repository()

            repository.upsert(a)
            repository.upsert(b)
            repository.upsert(c)

            assertEquals(listOf(a, b, c), repository.list())
            assertNotNull(fake.variantsJson)
            assertEquals(listOf(a, b, c), DeepLinkVariantsSerializer.decode(fake.variantsJson.orEmpty()))
        }

    @Test
    fun `upsert replaces a variant with the same id in place`() =
        runBlocking {
            val repository = repositoryWith(a, b, c)

            repository.upsert(b.copy(name = "Renamed"))

            assertEquals(listOf(a, b.copy(name = "Renamed"), c), repository.list())
        }

    @Test
    fun `duplicate inserts a named copy after the original`() =
        runBlocking {
            val repository = repositoryWith(a, b, c)
            val copy = b.copy(id = "new-id", name = "Product 123 (copy)")

            assertEquals(copy, repository.duplicate("b-id", "(copy)"))
            assertEquals(listOf(a, b, copy, c), repository.list())
        }

    @Test
    fun `duplicate of an unnamed variant stays unnamed`() =
        runBlocking {
            val repository = repositoryWith(a, b, c)

            assertNull(repository.duplicate("a-id", "(copy)")?.name)
        }

    @Test
    fun `duplicate of an unknown id returns null and changes nothing`() =
        runBlocking {
            val repository = repositoryWith(a, b, c)

            assertNull(repository.duplicate("missing", "(copy)"))
            assertEquals(listOf(a, b, c), repository.list())
        }

    @Test
    fun `delete removes the variant`() =
        runBlocking {
            val repository = repositoryWith(a, b, c)

            repository.delete("b-id")

            assertEquals(listOf(a, c), repository.list())
        }

    @Test
    fun `deleteAll returns the count and empties the list`() =
        runBlocking {
            val repository = repositoryWith(a, b, c)

            assertEquals(3, repository.deleteAll())
            assertEquals(emptyList<Variant>(), repository.list())
        }

    @Test
    fun `deleteAll on an empty list returns zero`() =
        runBlocking {
            assertEquals(0, repository().deleteAll())
        }

    @Test
    fun `find returns the match or null`() =
        runBlocking {
            val repository = repositoryWith(a, b, c)

            assertEquals(b, repository.find("b-id"))
            assertNull(repository.find("missing"))
        }

    @Test
    fun `failed write throws and keeps the state`() =
        runBlocking {
            val repository = repositoryWith(a)
            assertEquals(listOf(a), repository.list())
            fake.writeResult = false

            try {
                repository.upsert(b)
                fail("Expected VariantsWriteException")
            } catch (expected: VariantsWriteException) {
                assertEquals(listOf(a), repository.list())
            }
        }

    @Test
    fun `corrupt storage is backed up and treated as empty`() {
        fake.variantsJson = "{not json"

        assertEquals(emptyList<Variant>(), repository().list())
        assertEquals("{not json", fake.backup)
    }
}
