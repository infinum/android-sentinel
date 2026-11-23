package com.infinum.sentinel.domain.deeplinks

import com.infinum.sentinel.data.models.raw.deeplinks.DeepLinksData
import com.infinum.sentinel.data.models.raw.deeplinks.DomainVerificationData
import com.infinum.sentinel.data.models.raw.deeplinks.ManifestNodeData
import com.infinum.sentinel.data.models.raw.deeplinks.ManifestReadResult
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.ExampleLinkData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [DeepLinksRepository].
 */
internal class DeepLinksRepositoryTest {
    private class FakeCollector(
        private val data: DeepLinksData? = null,
        private val error: Throwable? = null,
    ) : Collectors.DeepLinks {
        var invocations = 0

        override fun invoke(): DeepLinksData {
            invocations++
            error?.let { throw it }
            return requireNotNull(data)
        }
    }

    private fun repository(collector: Collectors.DeepLinks) =
        DeepLinksRepository(
            collector = collector,
            verificationCollector =
                object : Collectors.DomainVerification {
                    override fun invoke(): DomainVerificationData? = null
                },
            packageName = "com.example.app",
            sdkInt = 37,
            ioDispatcher = Dispatchers.Unconfined,
        )

    private fun node(
        name: String,
        attributes: Map<String, String> = mapOf(),
        vararg children: ManifestNodeData,
    ) = ManifestNodeData(name, attributes, children.toList())

    private fun activity(
        name: String,
        label: String,
        host: String,
        path: String,
    ) = node(
        "activity",
        mapOf("name" to name, "exported" to "true", "label" to label),
        node(
            "intent-filter",
            mapOf(),
            node("action", mapOf("name" to "android.intent.action.VIEW")),
            node("category", mapOf("name" to "android.intent.category.DEFAULT")),
            node("category", mapOf("name" to "android.intent.category.BROWSABLE")),
            node("data", mapOf("scheme" to "https", "host" to host, "path" to path)),
        ),
    )

    private fun success(vararg activities: ManifestNodeData) =
        ManifestReadResult.Success(
            node("manifest", mapOf(), node("application", mapOf(), *activities)),
        )

    @Test
    fun `manifest failure keeps examples and flags the discovery as failed`() =
        runBlocking {
            val collector =
                FakeCollector(
                    DeepLinksData(
                        ManifestReadResult.Failure(IllegalStateException()),
                        listOf(ExampleLinkData("myapp://home", "Home")),
                    ),
                )

            val result = repository(collector).discover()

            assertEquals(true, result.isDiscoveryFailed)
            assertTrue(result.screens.isEmpty())
            assertEquals(listOf("myapp://home"), result.examples.map { it.displayText })
        }

    @Test
    fun `collector that throws gives an empty failed discovery`() =
        runBlocking {
            val result = repository(FakeCollector(error = IllegalStateException())).discover()

            assertEquals(true, result.isDiscoveryFailed)
            assertTrue(result.screens.isEmpty())
            assertTrue(result.examples.isEmpty())
        }

    @Test
    fun `successful manifest gives declared screens`() =
        runBlocking {
            val manifest =
                success(activity("com.example.app.DeepLinkActivity", "Deep link", "example.com", "/offers"))
            val collector = FakeCollector(DeepLinksData(manifest, listOf()))

            val result = repository(collector).discover()

            assertEquals(false, result.isDiscoveryFailed)
            assertEquals(1, result.screens.size)
            assertEquals("Deep link", result.screens[0].title)
            assertEquals(listOf("https://example.com/offers"), result.screens[0].links.map { it.displayText })
        }

    @Test
    fun `discover twice invokes the collector once and returns equal results`() =
        runBlocking {
            val collector = FakeCollector(DeepLinksData(success(), listOf()))
            val repository = repository(collector)

            val first = repository.discover()
            val second = repository.discover()

            assertEquals(1, collector.invocations)
            assertEquals(first, second)
        }

    @Test
    fun `same link on two screens is flagged as duplicate on both`() =
        runBlocking {
            val manifest =
                success(
                    activity("com.example.app.MainActivity", "Main", "example.com", "/a"),
                    activity("com.example.app.PromoActivity", "Promo", "example.com", "/a"),
                )
            val collector = FakeCollector(DeepLinksData(manifest, listOf()))

            val result = repository(collector).discover()

            assertEquals(
                setOf(ConfigurationWarning.DuplicateDeclaration(listOf("Promo"))),
                result.screens[0].links[0].warnings,
            )
            assertEquals(
                setOf(ConfigurationWarning.DuplicateDeclaration(listOf("Main"))),
                result.screens[1].links[0].warnings,
            )
        }
}
