package com.infinum.sentinel.annotations

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Strategy for sending captured annotations somewhere.
 *
 * Implement this to route notes to a server, a file, an issue tracker or a coding agent, then pass
 * it to `AnnotationsTool`. Sentinel serializes the notes itself and hands over the finished JSON,
 * so the note model can gain fields without breaking implementations.
 *
 * Return a short status message to show the user. Throw to signal failure. The screen then
 * reports `Export failed: ...` and keeps the notes pending. Implementations stay UI free.
 */
public interface AnnotationExporter {
    /** Drawable resource for the export button's icon. */
    @DrawableRes
    public fun icon(): Int

    /** String resource for the export button's label. */
    @StringRes
    public fun label(): Int

    /**
     * Exports [json], a JSON array with the [count] notes that have not been exported yet.
     *
     * Called on the main thread. Move blocking work, such as network or disk access, to another
     * dispatcher, for example with `withContext(Dispatchers.IO)`.
     */
    public suspend fun export(context: Context, json: String, count: Int): String
}
