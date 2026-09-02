package com.infinum.sentinel.domain.annotations

import android.content.Context
import com.infinum.sentinel.data.models.local.annotations.Annotation

@Suppress("unused", "UNUSED_PARAMETER")
public class ClaudeRoutineExporter(
    token: () -> String?,
    fireUrl: String,
) : AnnotationExporter {
    override fun icon(): Int = 0

    override fun label(): Int = 0

    override suspend fun export(context: Context, annotations: List<Annotation>): String = ""
}
