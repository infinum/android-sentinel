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
     * Throws when there is no `ClipboardManager`. The caller marks the notes as sent when this
     * returns, so returning without copying would flag them as delivered with nothing copied.
     */
    override suspend fun export(context: Context, json: String, count: Int): String {
        val clipboard =
            checkNotNull(context.getSystemService<ClipboardManager>()) { "Clipboard is unavailable" }
        clipboard.setPrimaryClip(ClipData.newPlainText(CLIP_LABEL, json))
        return context.resources.getQuantityString(R.plurals.sentinel_annotations_copied, count, count)
    }
}
