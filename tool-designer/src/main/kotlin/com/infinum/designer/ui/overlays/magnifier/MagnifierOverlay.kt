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
import com.infinum.designer.ui.models.configuration.MagnifierConfiguration
import com.infinum.designer.ui.overlays.AbstractOverlay
import com.infinum.designer.ui.utils.BitmapUtils
import com.infinum.designer.ui.utils.MediaProjectionHelper
import com.infinum.designer.ui.utils.ViewUtils
import com.infinum.designer.ui.views.magnifier.MagnifierView
import kotlin.math.roundToInt

class MagnifierOverlay(
    private val context: Context
) : AbstractOverlay<MagnifierConfiguration>(context) {

    private var configuration: MagnifierConfiguration = MagnifierConfiguration()

    private var view: MagnifierView? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var magnifierParams: WindowManager.LayoutParams? = null

    private var previewArea: Rect = Rect()

    private val screenCaptureLock = Any()

    private val magnifierWidth: Int =
        context.resources.getDimensionPixelSize(R.dimen.designer_magnifier_size)
    private val magnifierHeight: Int =
        context.resources.getDimensionPixelSize(R.dimen.designer_magnifier_size)

    private var previewSampleWidth =
        context.resources.getInteger(R.integer.designer_magnifier_sample_size)
    private var previewSampleHeight =
        context.resources.getInteger(R.integer.designer_magnifier_sample_size)

    private var isMoving: Boolean = false

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

        view = DesignerLayoutMagnifierBinding.inflate(LayoutInflater.from(context)).root
            .apply {
                setOnTouchListener { view, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            view.alpha = 0.5f
                            isMoving = false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            isMoving = true
                            updateMagnifierView(
                                event.rawX.roundToInt(),
                                event.rawY.roundToInt()
                            )
                        }
                        MotionEvent.ACTION_UP -> {
                            isMoving = false
                            view.alpha = 1.0f
                        }
                    }
                    true
                }
            }
        if (view?.isAttachedToWindow?.not() == true) {
            magnifierParams?.let { windowManager.addView(view, it) }
        }

        showing = true
    }

    override fun hide() {
        view?.let { removeViewIfAttached(it) }
        closeImageReader()
        teardownMediaProjection()

        showing = false
    }

    override fun update(configuration: MagnifierConfiguration) {
        this.configuration = configuration

        view?.setColorValueType(configuration.colorModel)
    }

    override fun reset(configuration: MagnifierConfiguration) {
        this.configuration = configuration
        hide()
        show()
    }

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
            this::acquireImage,
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

    private fun acquireImage(reader: ImageReader?) {
        synchronized(screenCaptureLock) {
            reader?.acquireLatestImage()
                ?.let { image ->
                    if (isMoving) {
                        view?.setPixels(
                            BitmapUtils.screenBitmapRegion(
                                image,
                                previewArea
                            )
                        )
                    }
                    image.close()
                }
        }
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
            windowManager.updateViewLayout(view, it)
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