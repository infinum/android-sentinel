package com.infinum.sentinel.annotations

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Strategy for sending captured annotations somewhere.
 *
 * Implement this to route notes to a server, a file or an issue tracker. The full
 * [SentinelAnnotation] objects are passed in so an implementation controls its own serialization;
 * [SentinelAnnotation.toExportJson] produces the default payload.
 *
 * Return a short status message to show the user. Throw to signal failure — the screen
 * reports it as `Export failed: ...`. Implementations stay UI free.
 */
public interface AnnotationExporter {
    /** Drawable resource for the export button's icon. */
    @DrawableRes
    public fun icon(): Int

    /** String resource for the export button's label. */
    @StringRes
    public fun label(): Int

    public suspend fun export(context: Context, annotations: List<SentinelAnnotation>): String
}
