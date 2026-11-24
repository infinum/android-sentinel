package com.infinum.sentinel.domain.deeplinks.discovery

import com.infinum.sentinel.data.models.raw.deeplinks.ManifestNodeData
import com.infinum.sentinel.domain.deeplinks.models.DeclaredData
import com.infinum.sentinel.domain.deeplinks.models.DeclaredFilter
import com.infinum.sentinel.domain.deeplinks.models.DeclaredGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [ManifestFilterExtractor].
 */
internal class ManifestFilterExtractorTest {
    private val view = "android.intent.action.VIEW"
    private val default = "android.intent.category.DEFAULT"
    private val browsable = "android.intent.category.BROWSABLE"

    private fun node(
        name: String,
        attributes: Map<String, String> = mapOf(),
        children: List<ManifestNodeData> = listOf(),
    ): ManifestNodeData = ManifestNodeData(name, attributes, children)

    private fun data(vararg attributes: Pair<String, String>): ManifestNodeData = node("data", attributes.toMap())

    private fun filter(
        vararg children: ManifestNodeData,
        action: String = view,
    ): ManifestNodeData =
        node(
            "intent-filter",
            children =
                listOf(
                    node("action", mapOf("name" to action)),
                    node("category", mapOf("name" to default)),
                    node("category", mapOf("name" to browsable)),
                ) + children,
        )

    private fun httpsFilter(): ManifestNodeData = filter(data("scheme" to "https", "host" to "example.com"))

    private fun activity(
        name: String,
        vararg children: ManifestNodeData,
        attributes: Map<String, String> = mapOf(),
    ): ManifestNodeData = node("activity", mapOf("name" to name) + attributes, children.toList())

    private fun manifest(vararg components: ManifestNodeData): ManifestNodeData =
        node("manifest", children = listOf(node("application", children = components.toList())))

    private fun extract(
        vararg components: ManifestNodeData,
        packageName: String = "com.example.app",
    ): List<DeclaredFilter> = ManifestFilterExtractor.extract(manifest(*components), packageName)

    private fun extractFilter(vararg children: ManifestNodeData): List<DeclaredFilter> =
        extract(activity("com.example.app.A", filter(*children)))

    @Test
    fun `extract reads a complete filter`() {
        val result =
            extract(
                activity(
                    "com.example.app.DeepLinkActivity",
                    filter(data("scheme" to "https", "host" to "example.com", "path" to "/a")),
                    attributes = mapOf("label" to "Deep link", "exported" to "true"),
                ),
            )

        assertEquals(
            listOf(
                DeclaredFilter(
                    componentName = "com.example.app.DeepLinkActivity",
                    title = "Deep link",
                    categories = listOf(default, browsable),
                    data = listOf(DeclaredData(scheme = "https", host = "example.com", path = "/a")),
                    groups = listOf(),
                ),
            ),
            result,
        )
    }

    @Test
    fun `extract resolves a leading dot name`() {
        assertEquals(
            "com.example.app.DeepLinkActivity",
            extract(activity(".DeepLinkActivity", httpsFilter())).single().componentName,
        )
    }

    @Test
    fun `extract resolves a name without dots`() {
        assertEquals("com.example.app.Foo", extract(activity("Foo", httpsFilter())).single().componentName)
    }

    @Test
    fun `extract uses the class name as title when there is no label`() {
        assertEquals("DeepLinkActivity", extract(activity(".DeepLinkActivity", httpsFilter())).single().title)
    }

    @Test
    fun `extract drops a mime type filter`() {
        assertTrue(extractFilter(data("scheme" to "https", "host" to "a.com", "mimeType" to "image/png")).isEmpty())
    }

    @Test
    fun `extract drops a mime group filter`() {
        assertTrue(extractFilter(data("scheme" to "https", "host" to "a.com", "mimeGroup" to "g")).isEmpty())
    }

    @Test
    fun `extract drops a content scheme filter`() {
        assertTrue(extractFilter(data("scheme" to "content", "host" to "a.com")).isEmpty())
    }

    @Test
    fun `extract drops a file scheme filter`() {
        assertTrue(extractFilter(data("scheme" to "file", "host" to "a.com")).isEmpty())
    }

    @Test
    fun `extract keeps the other schemes of a filter that also declares content or file`() {
        val result = extractFilter(data("scheme" to "https", "host" to "a.com"), data("scheme" to "content"), data("scheme" to "file")).single()

        assertEquals(listOf("https"), result.data.mapNotNull { it.scheme })
        assertEquals(listOf("a.com"), result.data.mapNotNull { it.host })
    }

    @Test
    fun `extract drops an opaque filter with ssp and no host`() {
        assertTrue(extractFilter(data("scheme" to "tel", "ssp" to "123")).isEmpty())
    }

    @Test
    fun `extract drops a filter that is not VIEW`() {
        val main = filter(data("scheme" to "https", "host" to "a.com"), action = "android.intent.action.MAIN")

        assertTrue(extract(activity("com.example.app.A", main)).isEmpty())
    }

    @Test
    fun `extract drops a filter without scheme`() {
        assertTrue(extractFilter(data("host" to "a.com")).isEmpty())
    }

    @Test
    fun `extract drops a component that is not exported`() {
        assertTrue(extract(activity("com.example.app.A", httpsFilter(), attributes = mapOf("exported" to "false"))).isEmpty())
    }

    @Test
    fun `extract keeps a component that opts out of the Bundle monitor`() {
        val optOut = node("meta-data", mapOf("name" to "infinum.sentinel.monitored", "value" to "false"))

        assertEquals(1, extract(activity("com.example.app.A", httpsFilter(), optOut)).size)
    }

    @Test
    fun `extract drops Sentinel UI components`() {
        assertTrue(extract(activity("com.infinum.sentinel.ui.main.SentinelActivity", httpsFilter())).isEmpty())
    }

    @Test
    fun `extract keeps the sample app`() {
        val result =
            extract(
                activity("com.infinum.sentinel.sample.MainActivity", httpsFilter()),
                packageName = "com.infinum.sentinel.sample",
            )

        assertEquals(1, result.size)
    }

    @Test
    fun `extract keeps a component without exported attribute`() {
        assertEquals(1, extract(activity("com.example.app.A", httpsFilter())).size)
    }

    @Test
    fun `extract keeps a component that opts in to monitoring`() {
        val optIn = node("meta-data", mapOf("name" to "infinum.sentinel.monitored", "value" to "true"))

        assertEquals(1, extract(activity("com.example.app.A", httpsFilter(), optIn)).size)
    }

    @Test
    fun `extract keeps ssp together with a host`() {
        assertEquals(1, extractFilter(data("scheme" to "https", "host" to "a.com", "ssp" to "x")).size)
    }

    @Test
    fun `extract uses alias own name and title`() {
        val alias =
            node(
                "activity-alias",
                mapOf("name" to "com.example.app.Alias", "targetActivity" to "com.example.app.Target"),
                listOf(httpsFilter()),
            )

        val result = extract(alias).single()

        assertEquals("com.example.app.Alias", result.componentName)
        assertEquals("Alias", result.title)
    }

    @Test
    fun `extract reads groups and their allow flag`() {
        fun group(vararg attributes: Pair<String, String>) =
            node("uri-relative-filter-group", attributes.toMap(), listOf(data("path" to "/group")))

        val result =
            extractFilter(
                data("scheme" to "https", "host" to "a.com"),
                group(),
                group("allow" to "false"),
            ).single()

        assertEquals(
            listOf(
                DeclaredGroup(true, listOf(DeclaredData(path = "/group"))),
                DeclaredGroup(false, listOf(DeclaredData(path = "/group"))),
            ),
            result.groups,
        )
    }

    @Test
    fun `extract keeps manifest order of components and filters`() {
        val result =
            extract(
                activity("com.example.app.First", httpsFilter(), httpsFilter()),
                activity("com.example.app.Second", httpsFilter()),
            )

        assertEquals(
            listOf("com.example.app.First", "com.example.app.First", "com.example.app.Second"),
            result.map { it.componentName },
        )
    }
}
