package com.infinum.sentinel.domain.annotations

import android.content.Context
import com.infinum.sentinel.data.models.local.annotations.Annotation

@Suppress("unused")
public class ClipboardAnnotationExporter : AnnotationExporter {
    override fun icon(): Int = 0

    override fun label(): Int = 0

    @Suppress("UNUSED_PARAMETER")
    override suspend fun export(context: Context, annotations: List<Annotation>): String = ""
}
