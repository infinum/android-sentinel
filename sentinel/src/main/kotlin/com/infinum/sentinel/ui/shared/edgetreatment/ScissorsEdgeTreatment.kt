package com.infinum.sentinel.ui.shared.edgetreatment

import androidx.annotation.Px
import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.ShapePath

/**
 * Used as edge treatment for repeating triangle cutout
 *
 * @param count Number of triangles. Any amount value is valid, zero will be coerced to 1.
 * @property height Height of each triangle. Value needs to be provided in pixels
 * @param centerOrigin Determines if the array of triangles starts at base or at height. Can be true or false.
 */
internal class ScissorsEdgeTreatment(
    private val count: Int,
    @Px var height: Float,
    private val centerOrigin: Boolean,
) : EdgeTreatment() {
    override fun getEdgePath(
        length: Float,
        center: Float,
        interpolation: Float,
        shapePath: ShapePath,
    ) {
        val baseLength = length / count.coerceAtLeast(1)

        val startX1 = if (centerOrigin) 0.0f else -baseLength / 2.0f
        val startX2 = if (centerOrigin) baseLength / 2.0f else 0.0f

        for (i in 0..count) {
            shapePath.lineTo(startX1 + (i * baseLength), height * interpolation)
            shapePath.lineTo(startX2 + (i * baseLength), 0.0f)
        }
    }
}
