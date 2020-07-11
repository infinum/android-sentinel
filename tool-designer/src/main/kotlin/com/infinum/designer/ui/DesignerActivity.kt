package com.infinum.designer.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.infinum.designer.databinding.DesignerActivityDesignerBinding
import kotlin.math.roundToInt


@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DesignerActivity : FragmentActivity() {

    companion object {
        private const val REQUEST_CODE_IMAGE_PICKER_PORTRAIT = 222
        private const val REQUEST_CODE_IMAGE_PICKER_LANDSCAPE = 333
    }

    private lateinit var binding: DesignerActivityDesignerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DesignerActivityDesignerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGridOverlay()
        setupMockupOverlay()
        setupColorPicker()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMAGE_PICKER_PORTRAIT) {
            if (resultCode == Activity.RESULT_OK) {
                data?.data?.let { setMockupPortrait(it) }
            } else {
                showMessage("Cannot set portrait mockup")
            }
        } else if (requestCode == REQUEST_CODE_IMAGE_PICKER_LANDSCAPE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.data?.let { setMockupLandscape(it) }
            } else {
                showMessage("Cannot set landscape mockup")
            }
        }
    }

    private fun setupGridOverlay() {
        with(binding) {
            gridOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                // TODO: Toggle grid overlay
            }
            horizontalLineColorButton.setOnClickListener {
                // TODO: Open color picker
            }
            verticalLineColorButton.setOnClickListener {
                // TODO: Open color picker
            }
            horizontalGridSizeSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {

                }
                horizontalGridSizeValueLabel.text = "${value.roundToInt()}dp"
            }
            verticalGridSizeSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {

                }
                verticalGridSizeValueLabel.text = "${value.roundToInt()}dp"
            }
        }
    }

    private fun setupMockupOverlay() {
        with(binding) {
            mockupOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                // TODO: Toggle mockup overlay
            }
            mockupOpacitySlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {

                }
                mockupOpacityValueLabel.text = "${value.roundToInt()}%"
            }
            portraitMockup.setOnClickListener {
                openImagePicker(REQUEST_CODE_IMAGE_PICKER_PORTRAIT)
            }
            portraitMockup.setOnLongClickListener {
                clearPortraitMockup()
                true
            }
            landscapeMockup.setOnClickListener {
                openImagePicker(REQUEST_CODE_IMAGE_PICKER_LANDSCAPE)
            }
            landscapeMockup.setOnLongClickListener {
                clearLandscapeMockup()
                true
            }
            clearPortraitMockupButton.setOnClickListener {
                clearPortraitMockup()
            }
            clearLandscapeMockupButton.setOnClickListener {
                clearLandscapeMockup()
            }
        }
    }

    private fun setupColorPicker() {
        with(binding) {
            colorPickerSwitch.setOnCheckedChangeListener { _, isChecked ->
                // TODO: Toggle color picker
            }
        }
    }

    private fun openImagePicker(requestCode: Int) =
        startActivityForResult(
            Intent.createChooser(
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "image/*"
                },
                "Select image"
            ),
            requestCode
        )

    private fun setMockupPortrait(uri: Uri) {
        with(binding) {
            portraitMockup.setImageURI(uri)
            clearPortraitMockupButton.isVisible = true
        }
    }

    private fun setMockupLandscape(uri: Uri) {
        with(binding) {
            landscapeMockup.setImageURI(uri)
            clearLandscapeMockupButton.isVisible = true
        }
    }

    private fun clearPortraitMockup() {
        with(binding) {
            if (portraitMockup.drawable != null) {
                portraitMockup.setImageDrawable(null)
                clearPortraitMockupButton.isGone = true
                showMessage("Portrait mockup cleared")
            }
        }
    }

    private fun clearLandscapeMockup() {
        with(binding) {
            if (landscapeMockup.drawable != null) {
                landscapeMockup.setImageDrawable(null)
                clearLandscapeMockupButton.isGone = true
                showMessage("Landscape mockup cleared")
            }
        }
    }

    private fun showMessage(text: String) =
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
}
