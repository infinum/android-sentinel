package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.annotations.AnnotationExporter
import com.infinum.sentinel.annotations.ClipboardAnnotationExporter
import com.infinum.sentinel.ui.annotations.AnnotationsActivity

/**
 * Attaches free-text notes to elements on the running app and exports them.
 *
 * Ships enabled with [ClipboardAnnotationExporter]. Pass an instance to [Sentinel.watch] to
 * change the destination, for example
 * `AnnotationsTool(ClaudeRoutineExporter({ BuildConfig.CLAUDE_CODE_TOKEN }, fireUrl))`.
 */
public class AnnotationsTool(
    public val exporter: AnnotationExporter = ClipboardAnnotationExporter(),
) : Sentinel.Tool {
    @StringRes
    override fun name(): Int = R.string.sentinel_annotations

    override fun icon(): Int = R.drawable.sentinel_ic_annotations

    override fun listener(): View.OnClickListener =
        View.OnClickListener {
            it.context.startActivity(
                Intent(it.context, AnnotationsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                },
            )
        }
}
