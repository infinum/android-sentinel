package com.infinum.designer.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.infinum.designer.R
import com.infinum.designer.databinding.DesignerFragmentOverlayMockupBinding
import com.infinum.designer.ui.models.MockupOrientation
import com.infinum.designer.ui.models.configuration.MockupConfiguration
import com.infinum.designer.ui.utils.viewBinding
import kotlin.math.roundToInt

internal class MockupOverlayFragment :
    AbstractOverlayFragment<MockupConfiguration>(R.layout.designer_fragment_overlay_mockup) {

    override val binding: DesignerFragmentOverlayMockupBinding by viewBinding(
        DesignerFragmentOverlayMockupBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            mockupOpacitySlider.isEnabled = false
            portraitMockup.isEnabled = false
            landscapeMockup.isEnabled = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        MockupOrientation(requestCode)
            ?.let { orientation ->
                when (orientation) {
                    MockupOrientation.PORTRAIT -> {
                        when (resultCode) {
                            Activity.RESULT_OK -> data?.data?.let {
                                setMockupPortrait(it)
                            } ?: showMessage("Cannot set portrait mockup")
                            Activity.RESULT_CANCELED -> Unit
                            else -> showMessage("Cannot set portrait mockup")
                        }
                    }
                    MockupOrientation.LANDSCAPE -> {
                        when (resultCode) {
                            Activity.RESULT_OK -> data?.data?.let {
                                setMockupLandscape(it)
                            } ?: showMessage("Cannot set landscape mockup")
                            Activity.RESULT_CANCELED -> Unit
                            else -> showMessage("Cannot set landscape mockup")
                        }
                    }
                }
            }
    }

    override fun toggleUi(enabled: Boolean) {
        with(binding) {
            mockupOverlaySwitch.isEnabled = enabled

            decreaseMockupOpacityButton.isEnabled = enabled
            increaseMockupOpacityButton.isEnabled = enabled
            mockupOpacitySlider.isEnabled = enabled

            portraitMockup.isEnabled = enabled
            landscapeMockup.isEnabled = enabled

            clearPortraitMockupButton.isEnabled = enabled
            clearLandscapeMockupButton.isEnabled = enabled
        }
    }

    override fun configure(configuration: MockupConfiguration) {
        with(binding) {
            mockupOverlaySwitch.setOnCheckedChangeListener(null)
            mockupOverlaySwitch.isChecked = configuration.enabled
            mockupOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                serviceActivity?.toggleMockup(isChecked)
            }
            decreaseMockupOpacityButton.setOnClickListener {
                mockupOpacitySlider.value =
                    (mockupOpacitySlider.value - mockupOpacitySlider.stepSize)
                        .coerceAtLeast(mockupOpacitySlider.valueFrom)
            }
            increaseMockupOpacityButton.setOnClickListener {
                mockupOpacitySlider.value =
                    (mockupOpacitySlider.value + mockupOpacitySlider.stepSize)
                        .coerceAtMost(mockupOpacitySlider.valueTo)
            }
            mockupOpacitySlider.clearOnChangeListeners()
            mockupOpacitySlider.addOnChangeListener { _, value, _ ->
                serviceActivity?.updateMockupOpacity(value / 100.0f)
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

            mockupOpacityValueLabel.text = "${(configuration.opacity * 100).roundToInt()}%"
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

    private fun setMockupPortrait(uri: Uri) {
        with(binding) {
            portraitMockup.setImageURI(uri)
            clearPortraitMockupButton.isVisible = true
        }

        serviceActivity?.updateMockupPortraitUri(uri.toString())
    }

    private fun setMockupLandscape(uri: Uri) {
        with(binding) {
            landscapeMockup.setImageURI(uri)
            clearLandscapeMockupButton.isVisible = true
        }

        serviceActivity?.updateMockupLandscapeUri(uri.toString())
    }

    private fun clearPortraitMockup() {
        with(binding) {
            if (portraitMockup.drawable != null) {
                portraitMockup.setImageDrawable(null)
                clearPortraitMockupButton.isGone = true

                showMessage("Portrait mockup cleared")
            }
        }

        serviceActivity?.updateMockupPortraitUri(null)
    }

    private fun clearLandscapeMockup() {
        with(binding) {
            if (landscapeMockup.drawable != null) {
                landscapeMockup.setImageDrawable(null)
                clearLandscapeMockupButton.isGone = true

                showMessage("Landscape mockup cleared")
            }
        }

        serviceActivity?.updateMockupLandscapeUri(null)
    }
}