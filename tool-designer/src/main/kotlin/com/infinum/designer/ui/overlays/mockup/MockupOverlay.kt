package com.infinum.designer.ui.overlays.mockup

import android.content.Context
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import com.infinum.designer.ui.overlays.AbstractOverlay
import com.infinum.designer.ui.utils.ViewUtils
import com.infinum.designer.ui.views.mockup.MockupView

class MockupOverlay(
    private val context: Context
) : AbstractOverlay(context) {

    private var mockOverlayView: MockupView? = null

    private var mockupOpacity: Float = 0.0f
    private var mockupPortraitUri: Uri? = null
    private var mockupLandscapeUri: Uri? = null

    override fun show() {
        mockOverlayView = MockupView(context)

        mockOverlayView?.let {
            it.updateOpacity(mockupOpacity)
            it.updatePortraitUri(mockupPortraitUri)
            it.updateLandscapeUri(mockupLandscapeUri)

            windowManager.addView(
                it,
                WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewUtils.getWindowType(),
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSPARENT
                )
            )
        }

        showing = true
    }

    override fun hide() {
        mockOverlayView?.let { removeViewIfAttached(it) }
        mockOverlayView = null

        showing = false
    }

    override fun update(bundle: Bundle) {
        mockupOpacity = bundle.getFloat("opacity", 0.0f)
        mockupPortraitUri = run {
            val uri = bundle.getString("portraitUri", "")
            if (uri.isNullOrBlank().not()) {
                Uri.parse(uri)
            } else {
                null
            }
        }
        mockupLandscapeUri = run {
            val uri = bundle.getString("landscapeUri", "")
            if (uri.isNullOrBlank().not()) {
                Uri.parse(uri)
            } else {
                null
            }
        }

        mockOverlayView?.let {
            it.updateOpacity(mockupOpacity)
            it.updatePortraitUri(mockupPortraitUri)
            it.updateLandscapeUri(mockupLandscapeUri)
        }
    }

    override fun reset() {
        mockupOpacity = 0.2f
        mockupPortraitUri = null
        mockupLandscapeUri = null
        super.reset()
    }
}