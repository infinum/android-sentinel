package com.infinum.sentinel.annotations

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

public interface AnnotationExporter {
    @DrawableRes
    public fun icon(): Int

    @StringRes
    public fun label(): Int

    public suspend fun export(context: Context, annotations: List<SentinelAnnotation>): String
}
