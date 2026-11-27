package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.annotations.AnnotationExporter
import com.infinum.sentinel.annotations.ClipboardAnnotationExporter

@Suppress("unused")
public class AnnotationsTool(
    public val exporter: AnnotationExporter = ClipboardAnnotationExporter(),
) : Sentinel.Tool {
    override fun name(): Int = 0

    override fun icon(): Int = 0

    override fun listener(): View.OnClickListener = View.OnClickListener { }
}
