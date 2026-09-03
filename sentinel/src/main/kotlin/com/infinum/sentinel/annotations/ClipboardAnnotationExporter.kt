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

    /**
     * Throws when there is no `ClipboardManager`, rather than reporting a copy that never
     * happened: the caller marks every exported note as sent the moment this returns, so a
     * silent no-op would flag the whole batch as delivered with nothing on the clipboard.
     */
    override suspend fun export(context: Context, annotations: List<SentinelAnnotation>): String {
        val clipboard =
            checkNotNull(context.getSystemService<ClipboardManager>()) { "Clipboard is unavailable" }
        clipboard.setPrimaryClip(ClipData.newPlainText(CLIP_LABEL, SentinelAnnotation.toExportJson(annotations)))
        return context.getString(R.string.sentinel_annotations_copied, annotations.size)
    }
}
