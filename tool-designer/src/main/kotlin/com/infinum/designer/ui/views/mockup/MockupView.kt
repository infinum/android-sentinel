package com.infinum.designer.ui.views.mockup

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class MockupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var opacity: Float = 0.2f

    private var portraitUri: Uri? = null
    private var landscapeUri: Uri? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setOpacity()
        setImage()
    }

    fun updateOpacity(opacity: Float) {
        this.opacity = opacity
        setOpacity()
    }

    fun updatePortraitUri(uri: Uri?) {
        this.portraitUri = uri
        setImage()
    }

    fun updateLandscapeUri(uri: Uri?) {
        this.landscapeUri = uri
        setImage()
    }

    private fun setOpacity() {
        alpha = opacity
    }

    private fun setImage() {
        setImageURI(
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                portraitUri
            } else {
                landscapeUri
            }
        )
    }
}