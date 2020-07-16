package com.infinum.designer.ui.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.media.Image
import java.nio.ByteBuffer

object BitmapUtils {

    fun screenBitmapRegion(image: Image, region: Rect): Bitmap? {
        val maxX: Int = image.width - 1
        val maxY: Int = image.height - 1

        val width = region.width()
        val height = region.height()

        val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val planes: Array<Image.Plane> = image.planes
        val buffer: ByteBuffer = planes[0].buffer
        val rowStride: Int = planes[0].rowStride
        val pixelStride: Int = planes[0].pixelStride

        var color: Int
        var pixelX: Int
        var pixelY: Int
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixelX = region.left + x
                pixelY = region.top + y
                color = if (pixelX in 0..maxX && pixelY >= 0 && pixelY <= maxY) {
                    val index = pixelY * rowStride + pixelX * pixelStride
                    buffer.position(index)
                    Color.argb(
                        255,
                        buffer.get().toInt() and 0xff,
                        buffer.get().toInt() and 0xff,
                        buffer.get().toInt() and 0xff
                    )
                } else {
                    0
                }
                bitmap.setPixel(x, y, color)
            }
        }
        return bitmap
    }
}