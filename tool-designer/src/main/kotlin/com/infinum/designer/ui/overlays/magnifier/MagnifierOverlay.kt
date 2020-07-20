package com.infinum.designer.ui.overlays.magnifier

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import com.infinum.designer.R
import com.infinum.designer.databinding.DesignerLayoutMagnifierBinding
import com.infinum.designer.extensions.half
import com.infinum.designer.extensions.screenCenter
import com.infinum.designer.ui.DesignerService
import com.infinum.designer.ui.overlays.AbstractOverlay
import com.infinum.designer.ui.utils.BitmapUtils
import com.infinum.designer.ui.utils.MediaProjectionHelper
import com.infinum.designer.ui.utils.ViewUtils
import com.infinum.designer.ui.views.magnifier.MagnifierView
import kotlin.math.roundToInt

class MagnifierOverlay(
    private val context: Context
) : AbstractOverlay(context) {

    private var magnifierView: MagnifierView? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var magnifierParams: WindowManager.LayoutParams? = null

    private var previewArea: Rect = Rect()

    private val screenCaptureLock = Any()

    private val magnifierWidth: Int =
        context.resources.getDimensionPixelSize(R.dimen.designer_picker_magnifying_ring_width)
    private val magnifierHeight: Int =
        context.resources.getDimensionPixelSize(R.dimen.designer_picker_magnifying_ring_height)

    private var previewSampleWidth =
        context.resources.getInteger(R.integer.designer_color_picker_sample_width)
    private var previewSampleHeight =
        context.resources.getInteger(R.integer.designer_color_picker_sample_height)

    @SuppressLint("ClickableViewAccessibility")
    override fun show() {
        setupMediaProjection()

        magnifierParams = WindowManager.LayoutParams(
            magnifierWidth,
            magnifierHeight,
            ViewUtils.getWindowType(true),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = Gravity.TOP xor Gravity.START
            with(context.screenCenter()) {
                this@apply.x = this.x - magnifierWidth.half()
                this@apply.y = this.y - magnifierHeight.half()
            }
        }

        magnifierView =
            DesignerLayoutMagnifierBinding.inflate(LayoutInflater.from(context)).root
                .apply {
                    setOnTouchListener { view, event ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN -> Unit
                            MotionEvent.ACTION_MOVE -> updateMagnifierView(
                                event.rawX.roundToInt(),
                                event.rawY.roundToInt()
                            )
                            MotionEvent.ACTION_UP -> Unit
                        }
                        true
                    }
                }
        if (magnifierView?.isAttachedToWindow?.not() == true) {
            magnifierParams?.let { windowManager.addView(magnifierView, it) }
        }
        context.screenCenter().let {
            updatePreviewArea(
                it.x,
                it.y
            )
        }

        showing = true
    }

    override fun hide() {
        magnifierView?.let { removeViewIfAttached(it) }
        closeImageReader()
        teardownMediaProjection()

        showing = false
    }

    override fun update(bundle: Bundle) = Unit

    private fun setupMediaProjection() {
        val size = Point()
        windowManager.defaultDisplay.getRealSize(size)

        imageReader = ImageReader.newInstance(
            size.x,
            size.y,
            PixelFormat.RGBA_8888,
            2
        )
        imageReader?.setOnImageAvailableListener(
            { reader ->
                synchronized(screenCaptureLock) {
                    reader.acquireNextImage()
                        .let { image ->
                            magnifierView?.setPixels(
                                BitmapUtils.screenBitmapRegion(
                                    image,
                                    previewArea
                                )
                            )
                            image.close()
                        }
                }
            },
            Handler()
        )

        // This line and command below needs to be tested
        mediaProjection = MediaProjectionHelper.data?.let {
            (context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)
                ?.getMediaProjection(
                    Activity.RESULT_OK,
                    it
                )
        }
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            DesignerService::class.java.simpleName,
            size.x,
            size.y,
            context.resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )
    }

    private fun teardownMediaProjection() {
        virtualDisplay?.release()
        mediaProjection?.stop()
    }

    private fun closeImageReader() {
        imageReader?.close()
        imageReader = null
    }

    private fun updateMagnifierView(x: Int, y: Int) {
        updatePreviewArea(x, y)

        magnifierParams?.let {
            it.x = x - magnifierWidth.half()
            it.y = y - magnifierHeight.half()
            windowManager.updateViewLayout(magnifierView, it)
        }
    }

    private fun updatePreviewArea(x: Int, y: Int) =
        with(previewArea) {
            left = x - previewSampleWidth.half()
            top = y - previewSampleHeight.half()
            right = x + previewSampleWidth.half() + 1
            bottom = y + previewSampleHeight.half() + 1
        }
}