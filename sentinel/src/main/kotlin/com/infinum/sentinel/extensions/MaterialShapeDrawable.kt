package com.infinum.sentinel.extensions

import android.content.Context
import android.graphics.Paint
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.infinum.sentinel.ui.shared.edgetreatment.CradleTopEdgeTreatment

@Suppress("LongParameterList")
internal fun MaterialShapeDrawable.toCradleDrawable(
    context: Context,
    @ColorRes color: Int,
    fabDiameter: Float,
    fabCradleMargin: Float,
    fabCornerRadius: Float,
    fabVerticalOffset: Float
): MaterialShapeDrawable {
    this.setTint(ContextCompat.getColor(context, color))
    this.paintStyle = Paint.Style.FILL
    this.shapeAppearanceModel = ShapeAppearanceModel.Builder()
        .setTopEdge(
            CradleTopEdgeTreatment(
                fabDiameter,
                fabCradleMargin,
                fabCornerRadius,
                fabVerticalOffset
            )
        )
        .build()
    return this
}
