package com.infinum.sentinel.domain.deeplinks.discovery

import com.infinum.sentinel.data.models.raw.deeplinks.ManifestNodeData
import com.infinum.sentinel.domain.deeplinks.models.DeclaredData
import com.infinum.sentinel.domain.deeplinks.models.DeclaredFilter
import com.infinum.sentinel.domain.deeplinks.models.DeclaredGroup

/**
 * Picks the intent filters that can open a link from the manifest tree.
 */
internal object ManifestFilterExtractor {
    private const val TAG_APPLICATION = "application"
    private const val TAG_ACTIVITY = "activity"
    private const val TAG_ALIAS = "activity-alias"
    private const val TAG_INTENT_FILTER = "intent-filter"
    private const val TAG_ACTION = "action"
    private const val TAG_CATEGORY = "category"
    private const val TAG_DATA = "data"
    private const val TAG_GROUP = "uri-relative-filter-group"

    private const val ACTION_VIEW = "android.intent.action.VIEW"
    private const val SENTINEL_UI_PREFIX = "com.infinum.sentinel.ui."
    private const val SCHEME_CONTENT = "content"
    private const val SCHEME_FILE = "file"
    private const val VALUE_FALSE = "false"

    private val SSP_ATTRIBUTES = listOf("ssp", "sspPrefix", "sspPattern", "sspAdvancedPattern", "sspSuffix")
    private val MIME_ATTRIBUTES = listOf("mimeType", "mimeGroup")

    /**
     * Returns the VIEW filters of exported components, in manifest order.
     *
     * Filters that describe no web or custom scheme link are dropped: content and file schemes,
     * mime type filters, filters without a scheme and opaque (scheme specific part only) links.
     */
    internal fun extract(
        root: ManifestNodeData,
        packageName: String,
    ): List<DeclaredFilter> =
        root.children
            .filter { it.name == TAG_APPLICATION }
            .flatMap { it.children }
            .filter { it.name == TAG_ACTIVITY || it.name == TAG_ALIAS }
            .flatMap { component -> extractComponent(component, packageName) }

    private fun extractComponent(
        component: ManifestNodeData,
        packageName: String,
    ): List<DeclaredFilter> {
        val componentName = resolveName(component.attributes["name"], packageName)
        return if (componentName == null || isExcluded(component, componentName)) {
            listOf()
        } else {
            val title =
                component.attributes["label"]?.takeIf { it.isNotBlank() }
                    ?: componentName.substringAfterLast('.')
            component.children
                .filter { it.name == TAG_INTENT_FILTER }
                .mapNotNull { toFilter(it, componentName, title) }
        }
    }

    /**
     * Mirrors PackageParser's class name building: a leading dot or a name without any dot
     * is relative to the package.
     */
    private fun resolveName(
        name: String?,
        packageName: String,
    ): String? =
        when {
            name.isNullOrEmpty() -> null
            name.startsWith(".") -> packageName + name
            !name.contains('.') -> "$packageName.$name"
            else -> name
        }

    /**
     * A missing `exported` counts as exported: before Android 12 that was the default for components
     * with filters. Sentinel's own UI is skipped by package prefix; the sample app shares the
     * `com.infinum.sentinel.` prefix and must stay. The `infinum.sentinel.monitored` meta-data is the
     * public opt-out of the Bundle monitor and does not hide a host screen here.
     */
    private fun isExcluded(
        component: ManifestNodeData,
        componentName: String,
    ): Boolean =
        component.attributes["exported"] == VALUE_FALSE ||
            componentName.startsWith(SENTINEL_UI_PREFIX)

    private fun toFilter(
        filter: ManifestNodeData,
        componentName: String,
        title: String,
    ): DeclaredFilter? {
        val actions = filter.children.filter { it.name == TAG_ACTION }.mapNotNull { it.attributes["name"] }
        val dataNodes = filter.children.filter { it.name == TAG_DATA }
        return if (ACTION_VIEW in actions && isLinkFilter(dataNodes)) {
            DeclaredFilter(
                componentName = componentName,
                title = title,
                categories = filter.children.filter { it.name == TAG_CATEGORY }.mapNotNull { it.attributes["name"] },
                data = dataNodes.map(::toData),
                groups =
                filter.children.filter { it.name == TAG_GROUP }.map { group ->
                    DeclaredGroup(
                        isAllowed = group.attributes["allow"] != VALUE_FALSE,
                        data = group.children.filter { it.name == TAG_DATA }.map(::toData),
                    )
                },
            )
        } else {
            null
        }
    }

    private fun isLinkFilter(dataNodes: List<ManifestNodeData>): Boolean {
        val schemes = dataNodes.mapNotNull { it.attributes["scheme"] }
        val hasMime = dataNodes.any { node -> MIME_ATTRIBUTES.any { it in node.attributes } }
        val isFileLike = schemes.any { it.equals(SCHEME_CONTENT, true) || it.equals(SCHEME_FILE, true) }
        return schemes.isNotEmpty() && !hasMime && !isFileLike && !isOpaque(dataNodes)
    }

    private fun isOpaque(dataNodes: List<ManifestNodeData>): Boolean =
        dataNodes.any { node -> SSP_ATTRIBUTES.any { it in node.attributes } } &&
            dataNodes.none { "host" in it.attributes }

    private fun toData(node: ManifestNodeData): DeclaredData =
        DeclaredData(
            scheme = node.attributes["scheme"],
            host = node.attributes["host"],
            port = node.attributes["port"],
            path = node.attributes["path"],
            pathPrefix = node.attributes["pathPrefix"],
            pathPattern = node.attributes["pathPattern"],
            pathAdvancedPattern = node.attributes["pathAdvancedPattern"],
            pathSuffix = node.attributes["pathSuffix"],
        )
}
