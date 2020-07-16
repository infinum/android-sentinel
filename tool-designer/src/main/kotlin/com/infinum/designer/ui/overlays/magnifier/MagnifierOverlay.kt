package com.infinum.designer.ui.overlays.magnifier

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PointF
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
import android.view.animation.OvershootInterpolator
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.infinum.designer.R
import com.infinum.designer.databinding.DesignerColorPickerMagnifierBinding
import com.infinum.designer.extensions.half
import com.infinum.designer.extensions.orZero
import com.infinum.designer.ui.DesignerService
import com.infinum.designer.ui.listeners.DampeningTouchListener
import com.infinum.designer.ui.listeners.NodeAnimatorListener
import com.infinum.designer.ui.listeners.NodeTouchListener
import com.infinum.designer.ui.listeners.NodeUpdateAnimator
import com.infinum.designer.ui.overlays.AbstractOverlay
import com.infinum.designer.ui.utils.BitmapUtils
import com.infinum.designer.ui.utils.MediaProjectionHelper
import com.infinum.designer.ui.utils.ViewUtils
import com.infinum.designer.ui.views.magnifier.MagnifierNodeView
import com.infinum.designer.ui.views.magnifier.MagnifierView
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class MagnifierOverlay(
    private val context: Context
) : AbstractOverlay(context) {

    private var magnifierView: MagnifierView? = null
    private var magnifierNodeView: MagnifierNodeView? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var nodeParams: WindowManager.LayoutParams? = null
    private var magnifierParams: WindowManager.LayoutParams? = null

    private var previewArea: Rect = Rect()
    private val mScreenCaptureLock = Any()

    private var animating = false
    private var previewSampleWidth = 0
    private var previewSampleHeight = 0
    private var nodeToMagnifierDistance = 0f
    private var angle = Math.PI.toFloat() * 1.5f
    private var startPosition: PointF = PointF()
    private var lastPosition: PointF = PointF()

    override fun show() {
        setupMediaProjection()

        val magnifierWidth: Int =
            context.resources.getDimensionPixelSize(R.dimen.designer_picker_magnifying_ring_width)
        val magnifierHeight: Int =
            context.resources.getDimensionPixelSize(R.dimen.designer_picker_magnifying_ring_height)

        val nodeViewSize: Int =
            context.resources.getDimensionPixelSize(R.dimen.designer_picker_node_size)

        nodeParams = WindowManager.LayoutParams(
            nodeViewSize,
            nodeViewSize,
            ViewUtils.getWindowType(true),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        magnifierParams = WindowManager.LayoutParams(
            magnifierWidth, magnifierHeight,
            ViewUtils.getWindowType(true),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val x: Int = context.resources.displayMetrics.widthPixels.half()
        val y: Int = context.resources.displayMetrics.heightPixels.half()
        nodeParams?.let {
            it.x = x - nodeViewSize.half()
            it.y = y - nodeViewSize.half()
        }
        magnifierParams?.let {
            it.x = x - magnifierWidth.half()
            it.y = nodeParams?.y.orZero() - magnifierHeight + nodeViewSize.half()
        }

        magnifierView =
            DesignerColorPickerMagnifierBinding.inflate(LayoutInflater.from(context)).root
                .apply {
                    setOnTouchListener(DampeningTouchListener(
                        onActionDown = {
                            lastPosition[it.rawX] = it.rawY
                            startPosition[(nodeParams?.x.orZero()).toFloat()] =
                                nodeParams?.y?.toFloat().orZero()
                        },
                        onActionMove = { event: MotionEvent, dampeningFactor: Float ->
                            updateMagnifierViewPosition(
                                (startPosition.x + magnifierNodeView?.width.orZero()
                                    .half() + ((event.rawX - lastPosition.x) / dampeningFactor)).roundToInt(),
                                (startPosition.y + magnifierNodeView?.height.orZero()
                                    .half() + ((event.rawY - lastPosition.y) / dampeningFactor)).roundToInt(),
                                angle
                            )
                        }
                    ))
                }

        magnifierNodeView = MagnifierNodeView(context)
            .apply {
                setOnTouchListener(NodeTouchListener(
                    onActionDown = { magnifierNodeView?.alpha = 0.0f },
                    onActionMove = {
                        angle = atan2(
                            ((magnifierParams?.x.orZero()) + magnifierView?.width.orZero()
                                .half() - it.rawX).toDouble(),
                            ((magnifierParams?.y.orZero()) + magnifierView?.height.orZero()
                                .half() - it.rawY).toDouble()
                        ).toFloat()
                        updateMagnifierViewPosition(
                            it.rawX.roundToInt(),
                            it.rawY.roundToInt(),
                            angle
                        )
                    },
                    onActionOther = {
                        magnifierNodeView?.alpha = 1.0f
                    }
                ))
            }
        addOverlayViewsIfDetached()
        animateColorPickerIn()

        previewSampleWidth =
            context.resources.getInteger(R.integer.designer_color_picker_sample_width)
        previewSampleHeight =
            context.resources.getInteger(R.integer.designer_color_picker_sample_height)
        previewArea = Rect(
            x - previewSampleWidth.half(),
            y - previewSampleHeight.half(),
            x + previewSampleWidth.half() + 1,
            y + previewSampleHeight.half() + 1
        )

        nodeToMagnifierDistance =
            (min(magnifierWidth, magnifierHeight) + nodeViewSize * 2).half().toFloat()
        lastPosition = PointF()
        startPosition = PointF()
    }

    override fun hide() {
        removeOverlayViewsIfAttached()
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
                synchronized(mScreenCaptureLock) {
                    if (animating.not()) {
                        val newImage = reader?.acquireNextImage()
                        newImage?.let { image ->
                            magnifierView?.setPixels(
                                BitmapUtils.screenBitmapRegion(
                                    image,
                                    previewArea
                                )
                            )
                            image.close()
                        }
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

    private fun recreateMediaProjection() {
        teardownMediaProjection()
        setupMediaProjection()
    }

    private fun closeImageReader() {
        imageReader?.close()
        imageReader = null
    }

    private fun removeOverlayViewsIfAttached() {
        magnifierView?.let { removeViewIfAttached(it) }
        magnifierNodeView?.let { removeViewIfAttached(it) }
    }

    private fun addOverlayViewsIfDetached() {
        if (magnifierNodeView?.isAttachedToWindow?.not() == true) {
            nodeParams?.let { windowManager.addView(magnifierNodeView, it) }
        }
        if (magnifierView?.isAttachedToWindow?.not() == true) {
            magnifierParams?.let { windowManager.addView(magnifierView, it) }
        }
    }

    private fun animateColorPickerIn() {
        magnifierView?.let {
            it.scaleX = 0f
            it.scaleY = 0f
            it.isVisible = true
        }
        magnifierNodeView?.isGone = true

        val startX =
            magnifierParams?.x.orZero() + (magnifierParams?.width.orZero() - nodeParams?.width.orZero()).half()
        val startY =
            magnifierParams?.y.orZero() + (magnifierParams?.height.orZero() - nodeParams?.height.orZero()).half()
        val endX = nodeParams?.x.orZero()
        val endY = nodeParams?.y.orZero()
        nodeParams?.let {
            it.x = startX
            it.y = startY
            windowManager.updateViewLayout(magnifierNodeView, it)
        }

        val animator: ValueAnimator = ObjectAnimator.ofFloat(0f, 1f)
            .apply {
                duration =
                    context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                interpolator = OvershootInterpolator()
                addUpdateListener(NodeUpdateAnimator(
                    onFractionUpdate = { fraction ->
                        nodeParams?.let {
                            it.x = startX + (fraction * (endX - startX)).toInt()
                            it.y = startY + (fraction * (endY - startY)).toInt()
                            windowManager.updateViewLayout(magnifierNodeView, it)
                        }
                    }
                ))
                addListener(NodeAnimatorListener(
                    onStart = {
                        animating = true
                        magnifierNodeView?.isVisible = true
                    },
                    onEnd = {
                        animating = false
                    }
                ))
            }

        magnifierView?.let {
            it.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setInterpolator(OvershootInterpolator())
                .setDuration(
                    context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                )
                .withEndAction { animator.start() }
        }

        showing = true
    }

    private fun updateMagnifierViewPosition(x: Int, y: Int, angle: Float) {
        with(previewArea) {
            left = x - previewSampleWidth.half()
            top = y - previewSampleHeight.half()
            right = x + previewSampleWidth.half() + 1
            bottom = y + previewSampleHeight.half() + 1
        }

        nodeParams?.let {
            it.x = x - magnifierNodeView?.width.orZero().half()
            it.y = y - magnifierNodeView?.height.orZero().half()
            windowManager.updateViewLayout(magnifierNodeView, it)
        }

        magnifierParams?.let {
            it.x = (nodeToMagnifierDistance * cos(angle.toDouble()).toFloat() + x).toInt()
            -magnifierView?.width.orZero().half()
            it.y = (nodeToMagnifierDistance * sin(angle.toDouble()).toFloat() + y).toInt()
            -magnifierView?.height.orZero().half()
            windowManager.updateViewLayout(magnifierView, it)
        }
    }
}