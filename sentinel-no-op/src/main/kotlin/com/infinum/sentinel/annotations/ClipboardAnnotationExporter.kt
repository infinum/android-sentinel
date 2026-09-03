package com.infinum.sentinel.annotations

import android.content.Context

@Suppress("unused")
public class ClipboardAnnotationExporter : AnnotationExporter {
    override fun icon(): Int = 0

    override fun label(): Int = 0

    @Suppress("UNUSED_PARAMETER")
    override suspend fun export(context: Context, annotations: List<SentinelAnnotation>): String = ""
}
