package com.infinum.sentinel.annotations

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

/**
 * An immutable note attached to an element captured on the running app.
 *
 * Every field except `target` is part of the export payload an agent reads to locate the element
 * in source. Internal on purpose: exporters receive the JSON, so adding a field here does not
 * break binary compatibility.
 */
@Suppress("LongParameterList")
@Serializable
internal data class SentinelAnnotation(
    /** Session-unique id assigned by the cache. Links a fix back to its note. */
    val id: String,
    /** The free-text note the user typed. */
    val note: String,
    /** Which toolkit the target belongs to. */
    val toolkit: AnnotationToolkit,
    /** View class simple name, or the composable function name. */
    val targetType: String,
    /** Ancestor chain, broadest first, e.g. `CoordinatorLayout>MaterialCardView>MaterialButton`. */
    val targetPath: String,
    /** Resource entry name of the view id, greppable as `R.id.x` and `@+id/x`. Null for Compose. */
    val viewId: String? = null,
    /** `Modifier.testTag` value when set. Null for Views. */
    val testTag: String? = null,
    /** `file:line` of the composable. Null for Views and when tooling data is unavailable. */
    val sourceLocation: String? = null,
    /** 1-based position among same-type elements on the screen, top to bottom then left to right. */
    val ordinal: Int = 1,
    /** Total count of same-type elements on the screen: the `M` in "N of M". */
    val ordinalCount: Int = 1,
    /** Visible text or content description, used to disambiguate identical siblings. */
    val text: String? = null,
    /** Simple name of the Activity the target lives in. */
    val screen: String? = null,
    /** Simple name of the deepest Fragment containing the target. */
    val fragment: String? = null,
    /** Navigation destination route when androidx-navigation is present. */
    val route: String? = null,
    /** Screen x of the target's centre when captured. Anchors re-binding after navigation. */
    val rebindX: Int = 0,
    /** Screen y of the target's centre when captured. Anchors re-binding after navigation. */
    val rebindY: Int = 0,
    /** Whether this note has already been exported. */
    val isSent: Boolean = false,
) {
    /** Live handle to the annotated element. Never serialized. */
    @Transient
    var target: Any? = null

    fun toExportJson(): String = json.encodeToString(serializer(), this)

    companion object {
        private val json = Json { encodeDefaults = true }

        fun toExportJson(annotations: List<SentinelAnnotation>): String =
            json.encodeToString(ListSerializer, annotations)

        private val ListSerializer = kotlinx.serialization.builtins.ListSerializer(serializer())
    }
}
