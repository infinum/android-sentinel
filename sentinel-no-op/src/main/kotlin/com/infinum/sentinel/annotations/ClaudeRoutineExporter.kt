package com.infinum.sentinel.annotations

import android.content.Context

@Suppress("unused", "UNUSED_PARAMETER")
public class ClaudeRoutineExporter(
    token: () -> String?,
    fireUrl: String,
) : AnnotationExporter {
    override fun icon(): Int = 0

    override fun label(): Int = 0

    override suspend fun export(context: Context, annotations: List<SentinelAnnotation>): String = ""
}
