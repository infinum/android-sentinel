package com.infinum.sentinel.annotations

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

/**
 * An immutable note attached to an element captured on the running app.
 *
 * Every field except `target` is part of the export payload a Claude Code routine reads
 * to locate the element in source.
 *
 * Named `SentinelAnnotation` rather than `Annotation` so it does not shadow `kotlin.Annotation`
 * in a consumer's file — implementing [AnnotationExporter] would otherwise silently take the
 * unqualified name away from the language's own.
 */
@Suppress("LongParameterList")
@Serializable
public data class SentinelAnnotation(
    /** Session-unique id assigned by the cache. Links a fix back to its note. */
    public val id: String,
    /** The free-text note the user typed. */
    public val note: String,
    /** Which toolkit the target belongs to. */
    public val toolkit: AnnotationToolkit,
    /** View class simple name, or the composable function name. */
    public val targetType: String,
    /** Ancestor chain, broadest first, e.g. `CoordinatorLayout>MaterialCardView>MaterialButton`. */
    public val targetPath: String,
    /** Resource entry name of the view id, greppable as `R.id.x` and `@+id/x`. Null for Compose. */
    public val viewId: String? = null,
    /** `Modifier.testTag` value when set. Null for Views. */
    public val testTag: String? = null,
    /** `file:line` of the composable. Null for Views and when tooling data is unavailable. */
    public val sourceLocation: String? = null,
    /** 1-based position among same-type elements on the screen, top to bottom then left to right. */
    public val ordinal: Int = 1,
    /** Total count of same-type elements on the screen — the `M` in "N of M". */
    public val ordinalCount: Int = 1,
    /** Visible text or content description, used to disambiguate identical siblings. */
    public val text: String? = null,
    /** Simple name of the Activity the target lives in. */
    public val screen: String? = null,
    /** Simple name of the deepest Fragment containing the target. */
    public val fragment: String? = null,
    /** Navigation destination route when androidx-navigation is present. */
    public val route: String? = null,
    /** Screen x of the target's centre when captured. Anchors re-binding after navigation. */
    public val rebindX: Int = 0,
    /** Screen y of the target's centre when captured. Anchors re-binding after navigation. */
    public val rebindY: Int = 0,
    /** Whether this note has already been exported. */
    public val isSent: Boolean = false,
) {
    /** Live handle to the annotated element. Never serialized, never public. */
    @Transient
    internal var target: Any? = null

    public fun toExportJson(): String = json.encodeToString(serializer(), this)

    public companion object {
        private val json = Json { encodeDefaults = true }

        public fun toExportJson(annotations: List<SentinelAnnotation>): String =
            json.encodeToString(ListSerializer, annotations)

        private val ListSerializer = kotlinx.serialization.builtins.ListSerializer(serializer())
    }
}
