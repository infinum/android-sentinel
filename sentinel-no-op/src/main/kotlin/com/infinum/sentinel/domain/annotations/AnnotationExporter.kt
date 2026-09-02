package com.infinum.sentinel.domain.annotations

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.infinum.sentinel.data.models.local.annotations.Annotation

public interface AnnotationExporter {
    @DrawableRes
    public fun icon(): Int

    @StringRes
    public fun label(): Int

    public suspend fun export(context: Context, annotations: List<Annotation>): String
}
