package com.infinum.designer.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import com.infinum.designer.R
import com.infinum.designer.databinding.DesignerFragmentOverlayMagnifierBinding
import com.infinum.designer.ui.models.ColorModel
import com.infinum.designer.ui.models.PermissionRequest
import com.infinum.designer.ui.models.configuration.MagnifierConfiguration
import com.infinum.designer.ui.utils.MediaProjectionHelper
import com.infinum.designer.ui.utils.viewBinding

internal class MagnifierOverlayFragment :
    AbstractOverlayFragment<MagnifierConfiguration>(R.layout.designer_fragment_overlay_magnifier) {

    override val binding: DesignerFragmentOverlayMagnifierBinding by viewBinding(
        DesignerFragmentOverlayMagnifierBinding::bind
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        PermissionRequest(requestCode)
            ?.let { permission ->
                when (permission) {
                    PermissionRequest.MEDIA_PROJECTION -> {
                        when (resultCode) {
                            Activity.RESULT_OK -> {
                                MediaProjectionHelper.data = data

                                serviceActivity?.toggleMagnifier(true)
                            }
                            Activity.RESULT_CANCELED -> {
                                with(binding) {
                                    if (magnifierSwitch.isChecked) {
                                        magnifierSwitch.isChecked = false
                                    }
                                }
                            }
                            else -> Unit
                        }
                    }
                    else -> throw NotImplementedError()
                }
            }
    }

    override fun toggleUi(enabled: Boolean) {
        with(binding) {
            magnifierSwitch.isEnabled = enabled
            hexButton.isEnabled = enabled
            rgbButton.isEnabled = enabled
            hsvButton.isEnabled = enabled
            if (enabled) {
                hexButton.isChecked = true
                rgbButton.isChecked = false
                hsvButton.isChecked = false
                colorModelToggleGroup.check(R.id.hexButton)
            } else {
                hexButton.isChecked = enabled
                rgbButton.isChecked = enabled
                hsvButton.isChecked = enabled
                colorModelToggleGroup.check(View.NO_ID)
            }
        }
    }

    override fun configure(configuration: MagnifierConfiguration) {
        with(binding) {
            magnifierSwitch.setOnCheckedChangeListener(null)
            magnifierSwitch.isChecked = configuration.enabled
            magnifierSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startProjection()
                } else {
                    serviceActivity?.toggleMagnifier(isChecked)
                }
            }
            colorModelToggleGroup.clearOnButtonCheckedListeners()
            colorModelToggleGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                serviceActivity?.updateMagnifierColorModel(
                    when (checkedId) {
                        R.id.hexButton -> ColorModel.HEX
                        R.id.rgbButton -> ColorModel.RGB
                        R.id.hsvButton -> ColorModel.HSV
                        else -> ColorModel.HEX
                    }
                )
            }
        }
    }

    private fun startProjection() {
        (context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)
            ?.createScreenCaptureIntent()
            ?.let {
                startActivityForResult(it, PermissionRequest.MEDIA_PROJECTION.requestCode)
            }

    }
}