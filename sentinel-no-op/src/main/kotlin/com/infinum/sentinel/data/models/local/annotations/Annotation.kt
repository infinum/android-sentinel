package com.infinum.sentinel.data.models.local.annotations

@Suppress("LongParameterList", "unused", "FunctionOnlyReturningConstant")
public data class Annotation(
    public val id: String,
    public val note: String,
    public val toolkit: AnnotationToolkit,
    public val targetType: String,
    public val targetPath: String,
    public val viewId: String? = null,
    public val testTag: String? = null,
    public val sourceLocation: String? = null,
    public val ordinal: Int = 1,
    public val ordinalCount: Int = 1,
    public val text: String? = null,
    public val screen: String? = null,
    public val fragment: String? = null,
    public val route: String? = null,
    public val rebindX: Int = 0,
    public val rebindY: Int = 0,
    public val isSent: Boolean = false,
) {
    public fun toExportJson(): String = ""

    public companion object {
        @Suppress("UNUSED_PARAMETER")
        public fun toExportJson(annotations: List<Annotation>): String = ""
    }
}
