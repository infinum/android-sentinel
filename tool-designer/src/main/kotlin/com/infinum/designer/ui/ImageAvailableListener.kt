package com.infinum.designer.ui

import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class ImageAvailableListener(
    private val width: Int,
    private val height: Int,
    private val onNewBitmap: (Bitmap) -> Unit
) : ImageReader.OnImageAvailableListener {

    override fun onImageAvailable(reader: ImageReader) {
        var image: Image? = null
        val fos: FileOutputStream? = null
        var bitmap: Bitmap? = null
        try {
            //Now we can get the screen capture as image using the following code.
            image = reader.acquireLatestImage()
            if (image != null) {
                val planes: Array<Image.Plane> = image.planes
                val buffer: ByteBuffer = planes[0].buffer
                val pixelStride: Int = planes[0].pixelStride
                val rowStride: Int = planes[0].rowStride
                val rowPadding: Int = rowStride - pixelStride * width

                // create bitmap
                bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer.rewind())
                onNewBitmap(Bitmap.createScaledBitmap(bitmap, 144, 144, true))
//
//                    //It will save the file with current time and date
//                    val dateFormat = SimpleDateFormat("yyyy_MM_dd,HH_mm_ss")
//                    val currentTimeStamp: String = dateFormat.format(Date())
//                    val mPath: String = Environment.getExternalStorageDirectory().toString()
//                        .toString() + "/" + currentTimeStamp + ".jpg"
//                    val imageFile = File(mPath)
//                    val outputStream = FileOutputStream(imageFile)
//                    val quality = 100
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
//                    stopProjection()
//                    Toast.makeText(
//                        context,
//                        "Screen shot saved at $mPath",
//                        Toast.LENGTH_LONG
//                    ).show()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (ioe: IOException) {
                    ioe.printStackTrace()
                }
            }
            bitmap?.recycle()
            image?.close()
        }
    }
}