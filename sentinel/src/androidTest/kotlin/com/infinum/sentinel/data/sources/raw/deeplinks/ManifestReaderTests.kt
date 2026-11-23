package com.infinum.sentinel.data.sources.raw.deeplinks

import android.content.Context
import android.util.Log
import android.webkit.WebSettings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.infinum.sentinel.data.models.raw.deeplinks.ManifestNodeData
import com.infinum.sentinel.data.models.raw.deeplinks.ManifestReadResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

/**
 * Reads the androidTest APK's own compiled manifest and compares it with the fixtures declared in
 * src/androidTest/AndroidManifest.xml. Expected values are copied from that manifest by hand.
 */
@RunWith(AndroidJUnit4::class)
internal class ManifestReaderTests {
    companion object {
        private const val TAG = "ManifestReaderTests"
        private const val PACKAGE_NAME = "com.infinum.sentinel.test"
        private const val FIXTURES = "com.infinum.sentinel.data.sources.raw.deeplinks.fixtures"
        private const val LINKS_ACTIVITY = "$FIXTURES.FixtureLinksActivity"
        private const val HIDDEN_ACTIVITY = "$FIXTURES.FixtureHiddenActivity"
        private const val MARKED_ACTIVITY = "$FIXTURES.FixtureSentinelMarkedActivity"
        private const val ALIAS = "$FIXTURES.FixtureAlias"

        private const val VIEW = "android.intent.action.VIEW"
        private const val DEFAULT = "android.intent.category.DEFAULT"
        private const val BROWSABLE = "android.intent.category.BROWSABLE"

        private lateinit var context: Context
        private lateinit var root: ManifestNodeData

        @BeforeClass
        @JvmStatic
        fun readManifest() {
            context = ApplicationProvider.getApplicationContext()
            val result = ManifestReader(context).read()
            assertTrue("Expected Success but was $result", result is ManifestReadResult.Success)
            root = (result as ManifestReadResult.Success).root
        }

        private fun node(
            name: String,
            vararg attributes: Pair<String, String>,
            children: List<ManifestNodeData> = listOf(),
        ) = ManifestNodeData(name = name, attributes = mapOf(*attributes), children = children)

        private fun action(name: String) = node("action", "name" to name)

        private fun category(name: String) = node("category", "name" to name)

        private fun data(vararg attributes: Pair<String, String>) = node("data", *attributes)

        private fun filter(vararg children: ManifestNodeData) = node("intent-filter", children = children.toList())
    }

    private fun component(name: String): ManifestNodeData? =
        root.children
            .single { it.name == "application" }
            .children
            .find { it.attributes["name"] == name }

    @Test
    @SmallTest
    fun read_returnsThisPackagesBaseManifest() {
        assertEquals("manifest", root.name)
        assertEquals(PACKAGE_NAME, root.attributes["package"])
        assertFalse(root.attributes.containsKey("split"))
    }

    @Test
    @SmallTest
    fun read_keepsOnlyApplicationChildrenOfManifest() {
        assertEquals(listOf("application"), root.children.map { it.name })
    }

    @Test
    @SmallTest
    fun linksActivity_matchesFixtureTree() {
        val expected =
            node(
                "activity",
                "name" to LINKS_ACTIVITY,
                "exported" to "true",
                "label" to "Fixture links",
                children =
                    listOf(
                        filter(
                            action(VIEW),
                            category(DEFAULT),
                            category(BROWSABLE),
                            data("scheme" to "http"),
                            data("scheme" to "https"),
                            data("host" to "example.com"),
                            data("pathPrefix" to "/orders/"),
                        ),
                        filter(
                            action(VIEW),
                            category(DEFAULT),
                            data("scheme" to "sentineltest", "host" to "ref.example.com", "path" to "/item"),
                        ),
                        filter(
                            action(VIEW),
                            category(BROWSABLE),
                            data("scheme" to "https", "host" to "*.example.com", "pathPattern" to "/p/.*"),
                        ),
                        filter(
                            action(VIEW),
                            category(DEFAULT),
                            category(BROWSABLE),
                            data("scheme" to "https", "host" to "docs.example.com", "port" to "8443"),
                            data("pathSuffix" to ".pdf"),
                            data("pathAdvancedPattern" to "/a/[0-9]+"),
                        ),
                        filter(
                            action(VIEW),
                            category(DEFAULT),
                            category(BROWSABLE),
                            data("scheme" to "https", "host" to "group.example.com"),
                            node(
                                "uri-relative-filter-group",
                                "allow" to "true",
                                children =
                                    listOf(
                                        data("path" to "/group"),
                                        data("query" to "id=*"),
                                    ),
                            ),
                        ),
                        filter(
                            action(VIEW),
                            category(DEFAULT),
                            data("mimeType" to "image/*"),
                        ),
                    ),
            )

        assertEquals(expected, component(LINKS_ACTIVITY))
    }

    @Test
    @SmallTest
    fun stringReferenceHost_isResolved() {
        val hosts =
            component(LINKS_ACTIVITY)
                ?.children
                ?.flatMap { it.children }
                ?.mapNotNull { it.attributes["host"] }
                .orEmpty()

        assertTrue("Hosts were $hosts", "ref.example.com" in hosts)
    }

    @Test
    @SmallTest
    fun hiddenActivity_keepsExportedFalse() {
        val expected =
            node(
                "activity",
                "name" to HIDDEN_ACTIVITY,
                "exported" to "false",
                children =
                    listOf(
                        filter(
                            action(VIEW),
                            category(DEFAULT),
                            data("scheme" to "hiddentest", "host" to "hidden"),
                        ),
                    ),
            )

        assertEquals(expected, component(HIDDEN_ACTIVITY))
    }

    @Test
    @SmallTest
    fun sentinelMarkedActivity_skipsMetaData() {
        val expected =
            node(
                "activity",
                "name" to MARKED_ACTIVITY,
                "exported" to "true",
                children =
                    listOf(
                        filter(
                            action(VIEW),
                            category(DEFAULT),
                            data("scheme" to "markedtest", "host" to "marked"),
                        ),
                    ),
            )

        assertEquals(expected, component(MARKED_ACTIVITY))
    }

    @Test
    @SmallTest
    fun alias_keepsTargetActivity() {
        val expected =
            node(
                "activity-alias",
                "name" to ALIAS,
                "exported" to "true",
                "targetActivity" to LINKS_ACTIVITY,
                children =
                    listOf(
                        filter(
                            action(VIEW),
                            category(DEFAULT),
                            data("scheme" to "aliastest", "host" to "alias"),
                        ),
                    ),
            )

        assertEquals(expected, component(ALIAS))
    }

    @Test
    @SmallTest
    fun cookieScan_readsSameManifestAsResourceCookie() {
        val reader = ManifestReader(context)

        val fromResourceCookie = reader.readFromResourceCookie()
        val fromCookieScan = reader.readFromCookieScan()

        assertNotNull(fromResourceCookie)
        assertEquals(fromResourceCookie, fromCookieScan)
    }

    @Test
    @SmallTest
    fun webViewInitialization_doesNotChangeTree() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            Log.i(TAG, "WebView user agent: ${WebSettings.getDefaultUserAgent(context)}")
        }

        val result = ManifestReader(context).read()

        assertEquals(ManifestReadResult.Success(root), result)
        Log.i(TAG, "Cookie-less parser package after WebView init: ${cookieLessManifestPackage()}")
    }

    /**
     * Diagnostic only: the package of the manifest the cookie-less parser returns. Not asserted.
     */
    private fun cookieLessManifestPackage(): String? {
        val parser = context.assets.openXmlResourceParser("AndroidManifest.xml")
        try {
            var event = parser.next()
            while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT) {
                event = parser.next()
            }
            return if (event == XmlPullParser.START_TAG) parser.getAttributeValue(null, "package") else null
        } finally {
            parser.close()
        }
    }
}
