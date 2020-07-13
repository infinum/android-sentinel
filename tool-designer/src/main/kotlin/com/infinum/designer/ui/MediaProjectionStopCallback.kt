package com.infinum.designer.ui

import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler

class MediaProjectionStopCallback(
    private val handler: Handler,
    private val virtualDisplay: VirtualDisplay?,
    private val imageReader: ImageReader?,
    private val mediaProjection: MediaProjection?
) : MediaProjection.Callback() {

    override fun onStop() {
        handler.post {
            virtualDisplay?.release()
            imageReader?.setOnImageAvailableListener(null, null)
            mediaProjection?.unregisterCallback(this)
        }
    }
}