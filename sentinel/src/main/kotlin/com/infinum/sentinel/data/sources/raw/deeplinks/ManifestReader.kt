package com.infinum.sentinel.data.sources.raw.deeplinks

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.util.TypedValue
import androidx.annotation.VisibleForTesting
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.raw.deeplinks.ManifestNodeData
import com.infinum.sentinel.data.models.raw.deeplinks.ManifestReadResult
import me.tatarka.inject.annotations.Inject
import org.xmlpull.v1.XmlPullParser

/**
 * Reads the host app's compiled AndroidManifest.xml from its base APK.
 *
 * No public API lists an app's intent filters, so the reader opens the compiled manifest through
 * [android.content.res.AssetManager.openXmlResourceParser] with the asset cookie of the base APK.
 * The cookie comes from one of Sentinel's own layouts: library resources are merged into the base APK,
 * and unqualified layouts never go to config splits.
 *
 * Every parsed manifest is checked to belong to this package and not to be a split, because other
 * loaded asset paths (framework, shared libraries, the WebView provider) have manifests too.
 *
 * Only the elements the deep links tool needs are kept; see [KEPT_TAGS].
 * No SDK gating is applied here, so newer attributes appear even on older devices.
 *
 * The compiled manifest identifies `android:` attributes by resource id, and some build tools strip the
 * attribute name strings. So the names of the attributes the tool reads are looked up by id first, see
 * [ATTRIBUTE_NAMES], and read from the string pool only as a fallback.
 *
 * Reading parses a binary XML asset, so call it off the main thread.
 */
@Inject
internal class ManifestReader(
    private val context: Context,
) {
    @SuppressLint("InlinedApi")
    private companion object {
        const val MANIFEST_FILE = "AndroidManifest.xml"
        const val TAG_MANIFEST = "manifest"
        const val ATTRIBUTE_PACKAGE = "package"
        const val ATTRIBUTE_SPLIT = "split"
        const val MAX_COOKIE = 16

        val KEPT_TAGS =
            setOf(
                "application",
                "activity",
                "activity-alias",
                "intent-filter",
                "action",
                "category",
                "data",
                "uri-relative-filter-group",
            )

        /**
         * Names of the framework attributes the tool reads, by resource id. Framework attribute ids are stable
         * across platform versions, so ids from newer platforms are safe on older devices: they simply never
         * appear there. That is why InlinedApi is suppressed.
         */
        val ATTRIBUTE_NAMES: Map<Int, String> =
            mapOf(
                android.R.attr.name to "name",
                android.R.attr.label to "label",
                android.R.attr.exported to "exported",
                android.R.attr.enabled to "enabled",
                android.R.attr.scheme to "scheme",
                android.R.attr.host to "host",
                android.R.attr.port to "port",
                android.R.attr.path to "path",
                android.R.attr.pathPrefix to "pathPrefix",
                android.R.attr.pathPattern to "pathPattern",
                android.R.attr.pathAdvancedPattern to "pathAdvancedPattern",
                android.R.attr.pathSuffix to "pathSuffix",
                android.R.attr.mimeType to "mimeType",
                android.R.attr.mimeGroup to "mimeGroup",
                android.R.attr.ssp to "ssp",
                android.R.attr.sspPrefix to "sspPrefix",
                android.R.attr.sspPattern to "sspPattern",
                android.R.attr.sspAdvancedPattern to "sspAdvancedPattern",
                android.R.attr.sspSuffix to "sspSuffix",
                android.R.attr.allow to "allow",
            )
    }

    private val resources: Resources
        get() = context.resources

    /**
     * Reads the manifest, first with the cookie of Sentinel's own resources, then by scanning cookies.
     * The scan also runs when the first read throws. Never throws; any problem is returned as [ManifestReadResult.Failure].
     */
    fun read(): ManifestReadResult =
        try {
            val root = readFromResourceCookieOrNull() ?: readFromCookieScan()
            if (root == null) {
                ManifestReadResult.Failure(IllegalStateException("No manifest found for ${context.packageName}"))
            } else {
                ManifestReadResult.Success(root)
            }
        } catch (exception: Exception) {
            ManifestReadResult.Failure(exception)
        }

    /**
     * Like [readFromResourceCookie], but a failure counts as no manifest, so the caller falls back to the scan.
     */
    private fun readFromResourceCookieOrNull(): ManifestNodeData? =
        try {
            readFromResourceCookie()
        } catch (ignored: Exception) {
            null
        }

    /**
     * Reads the manifest of the APK that holds Sentinel's resources, or null if it isn't the base APK.
     */
    @VisibleForTesting
    fun readFromResourceCookie(): ManifestNodeData? {
        val typedValue = TypedValue()
        resources.getValue(R.layout.sentinel_fragment, typedValue, true)
        return readCookie(typedValue.assetCookie)
    }

    /**
     * Tries the first [MAX_COOKIE] asset cookies and returns the first manifest that is this package's base APK.
     */
    @VisibleForTesting
    fun readFromCookieScan(): ManifestNodeData? =
        (1..MAX_COOKIE)
            .asSequence()
            .mapNotNull { cookie -> readCookieOrNull(cookie) }
            .firstOrNull()

    private fun readCookieOrNull(cookie: Int): ManifestNodeData? =
        try {
            readCookie(cookie)
        } catch (ignored: Exception) {
            // Any failure, including XmlPullParserException, only rules this cookie out; the scan goes on.
            null
        }

    private fun readCookie(cookie: Int): ManifestNodeData? {
        val parser = resources.assets.openXmlResourceParser(cookie, MANIFEST_FILE)
        try {
            return parseBaseManifest(parser)
        } finally {
            parser.close()
        }
    }

    private fun parseBaseManifest(parser: XmlResourceParser): ManifestNodeData? {
        var event = parser.next()
        while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT) {
            event = parser.next()
        }
        val attributes = if (event == XmlPullParser.START_TAG) readAttributes(parser) else mapOf()
        val isBaseManifest =
            event == XmlPullParser.START_TAG &&
                parser.name == TAG_MANIFEST &&
                attributes[ATTRIBUTE_PACKAGE] == context.packageName &&
                !attributes.containsKey(ATTRIBUTE_SPLIT)
        return if (isBaseManifest) {
            ManifestNodeData(name = TAG_MANIFEST, attributes = attributes, children = readChildren(parser))
        } else {
            null
        }
    }

    /**
     * Reads child elements until the end tag of the current element. Expects the parser on a start tag.
     */
    private fun readChildren(parser: XmlResourceParser): List<ManifestNodeData> {
        val children = mutableListOf<ManifestNodeData>()
        var event = parser.next()
        while (event != XmlPullParser.END_TAG && event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                if (parser.name in KEPT_TAGS) {
                    val name = parser.name
                    val attributes = readAttributes(parser)
                    children += ManifestNodeData(name = name, attributes = attributes, children = readChildren(parser))
                } else {
                    skipElement(parser)
                }
            }
            event = parser.next()
        }
        return children
    }

    /**
     * Skips the current element and all its descendants. Expects the parser on a start tag.
     */
    private fun skipElement(parser: XmlResourceParser) {
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> depth = 0
            }
        }
    }

    private fun readAttributes(parser: XmlResourceParser): Map<String, String> =
        (0 until parser.attributeCount)
            .mapNotNull { index ->
                val name = attributeName(parser, index)
                if (name.isNullOrEmpty()) null else name to attributeValue(parser, index)
            }.toMap()

    private fun attributeName(
        parser: XmlResourceParser,
        index: Int,
    ): String? {
        val resourceId = parser.getAttributeNameResource(index)
        return (if (resourceId != 0) ATTRIBUTE_NAMES[resourceId] else null)
            ?: parser.getAttributeName(index)
    }

    /**
     * Returns the attribute value, resolving references such as `@string/host` or `@bool/flag`.
     * An unresolvable reference keeps its raw `@<id>` text.
     */
    private fun attributeValue(
        parser: XmlResourceParser,
        index: Int,
    ): String {
        val referenceId = parser.getAttributeResourceValue(index, 0)
        val raw = parser.getAttributeValue(index).orEmpty()
        return if (referenceId == 0) raw else resolveReference(referenceId) ?: raw
    }

    private fun resolveReference(referenceId: Int): String? =
        try {
            val typedValue = TypedValue()
            resources.getValue(referenceId, typedValue, true)
            typedValue.coerceToString()?.toString()
        } catch (ignored: Resources.NotFoundException) {
            null
        }
}
