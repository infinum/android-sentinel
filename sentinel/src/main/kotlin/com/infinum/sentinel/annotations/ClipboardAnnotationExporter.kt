package com.infinum.sentinel.annotations

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService
import com.infinum.sentinel.R

/**
 * Default exporter. Copies the export JSON to the system clipboard.
 */
public class ClipboardAnnotationExporter : AnnotationExporter {
    private companion object {
        private const val CLIP_LABEL = "Sentinel annotations"
    }

    override fun icon(): Int = R.drawable.sentinel_ic_copy

    override fun label(): Int = R.string.sentinel_annotations_copy

    override suspend fun export(context: Context, annotations: List<SentinelAnnotation>): String {
        val json = SentinelAnnotation.toExportJson(annotations)
        context.getSystemService<ClipboardManager>()
            ?.setPrimaryClip(ClipData.newPlainText(CLIP_LABEL, json))
        return context.getString(R.string.sentinel_annotations_copied, annotations.size)
    }
}
