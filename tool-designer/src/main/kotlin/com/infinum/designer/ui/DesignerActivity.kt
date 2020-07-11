package com.infinum.designer.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.infinum.designer.databinding.DesignerActivityDesignerBinding
import com.infinum.designer.databinding.DesignerViewColorPickerBinding
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlin.math.roundToInt

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DesignerActivity : FragmentActivity() {

    private lateinit var binding: DesignerActivityDesignerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DesignerActivityDesignerBinding.inflate(layoutInflater)
            .also { setContentView(it.root) }
            .also { binding = it }
            .also {
                setupGridOverlay()
                setupMockupOverlay()
                setupColorPicker()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (MockupOrientation(requestCode)) {
            MockupOrientation.PORTRAIT -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { setMockupPortrait(it) }
                        ?: showMessage("Cannot set portrait mockup")
                } else {
                    showMessage("Cannot set portrait mockup")
                }
            }
            MockupOrientation.LANDSCAPE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { setMockupLandscape(it) }
                        ?: showMessage("Cannot set landscape mockup")
                } else {
                    showMessage("Cannot set landscape mockup")
                }
            }
            else -> Unit
        }
    }

    private fun setupGridOverlay() =
        with(binding) {
            gridOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                // TODO: Toggle grid overlay
            }
            horizontalLineColorButton.setOnClickListener {
                openColorPicker(LineOrientation.HORIZONTAL)
            }
            verticalLineColorButton.setOnClickListener {
                openColorPicker(LineOrientation.VERTICAL)
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

    private fun setupMockupOverlay() =
        with(binding) {
            mockupOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                // TODO: Toggle mockup overlay
            }
            mockupOpacitySlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {

                }
                mockupOpacityValueLabel.text = "${value.roundToInt()}%"
            }

            portraitMockupLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio =
                    "${resources.displayMetrics.widthPixels}:${resources.displayMetrics.heightPixels}"
            }
            landscapeMockupLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio =
                    "${resources.displayMetrics.heightPixels}:${resources.displayMetrics.widthPixels}"
            }
            portraitMockup.setOnClickListener {
                openImagePicker(MockupOrientation.PORTRAIT)
            }
            portraitMockup.setOnLongClickListener {
                clearPortraitMockup()
                true
            }
            landscapeMockup.setOnClickListener {
                openImagePicker(MockupOrientation.LANDSCAPE)
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

    private fun setupColorPicker() =
        with(binding) {
            colorPickerSwitch.setOnCheckedChangeListener { _, isChecked ->
                // TODO: Toggle color picker
            }
        }

    private fun openColorPicker(orientation: LineOrientation) {
        DesignerViewColorPickerBinding.inflate(layoutInflater)
            .apply {
                colorPickerView.setColorListener(object : ColorEnvelopeListener {
                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        envelope?.let {
                            when (orientation) {
                                LineOrientation.HORIZONTAL -> setHorizontalGridLineColor(it.color, it.hexCode)
                                LineOrientation.VERTICAL -> setVerticalGridLineColor(it.color, it.hexCode)
                            }
                        }
                    }
                })
            }
            .let {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Select ${orientation.name.toLowerCase()} line color")
                    .setView(it.root)
                    .setNegativeButton("Close") { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
    }

    private fun openImagePicker(orientation: MockupOrientation) =
        startActivityForResult(
            Intent.createChooser(
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "image/*"
                },
                "Select image"
            ),
            orientation.requestCode
        )

    private fun setHorizontalGridLineColor(color: Int, code: String) {
        with(binding) {
            horizontalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            horizontalLineColorValueLabel.text = "#${code.drop(2)}"
        }
    }

    private fun setVerticalGridLineColor(color: Int, code: String) {
        with(binding) {
            verticalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            vertialLineColorValueLabel.text = "#${code.drop(2)}"
        }
    }

    private fun setMockupPortrait(uri: Uri) =
        with(binding) {
            portraitMockup.setImageURI(uri)
            clearPortraitMockupButton.isVisible = true
        }

    private fun setMockupLandscape(uri: Uri) =
        with(binding) {
            landscapeMockup.setImageURI(uri)
            clearLandscapeMockupButton.isVisible = true
        }

    private fun clearPortraitMockup() =
        with(binding) {
            if (portraitMockup.drawable != null) {
                portraitMockup.setImageDrawable(null)
                clearPortraitMockupButton.isGone = true
                showMessage("Portrait mockup cleared")
            }
        }

    private fun clearLandscapeMockup() =
        with(binding) {
            if (landscapeMockup.drawable != null) {
                landscapeMockup.setImageDrawable(null)
                clearLandscapeMockupButton.isGone = true
                showMessage("Landscape mockup cleared")
            }
        }

    private fun showMessage(text: String) =
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
}
