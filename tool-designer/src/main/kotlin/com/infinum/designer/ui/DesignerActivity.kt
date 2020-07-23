package com.infinum.designer.ui

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.annotation.RestrictTo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.infinum.designer.R
import com.infinum.designer.databinding.DesignerActivityDesignerBinding
import com.infinum.designer.databinding.DesignerViewColorPickerBinding
import com.infinum.designer.extensions.toPx
import com.infinum.designer.extensions.getHexCode
import com.infinum.designer.ui.models.ColorModel
import com.infinum.designer.ui.models.LineOrientation
import com.infinum.designer.ui.models.MockupOrientation
import com.infinum.designer.ui.models.PermissionRequest
import com.infinum.designer.ui.models.configuration.MagnifierConfiguration
import com.infinum.designer.ui.models.configuration.DesignerConfiguration
import com.infinum.designer.ui.models.configuration.GridConfiguration
import com.infinum.designer.ui.models.configuration.MockupConfiguration
import com.infinum.designer.ui.utils.MediaProjectionHelper
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlin.math.floor
import kotlin.math.roundToInt

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DesignerActivity : ServiceActivity() {

    private lateinit var binding: DesignerActivityDesignerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DesignerActivityDesignerBinding.inflate(layoutInflater)
            .also { setContentView(it.root) }
            .also { binding = it }
            .also {
                with(binding) {
                    overlayGridView.horizontalGridSizeSlider.isEnabled = false
                    overlayGridView.verticalGridSizeSlider.isEnabled = false
                    overlayMockupView.mockupOpacitySlider.isEnabled = false
                    overlayMockupView.portraitMockup.isEnabled = false
                    overlayMockupView.landscapeMockup.isEnabled = false
                }
                setupToolbar()
                setupUi(DesignerConfiguration())
                setupPermission()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        MockupOrientation(requestCode)
            ?.let { orientation ->
                when (orientation) {
                    MockupOrientation.PORTRAIT -> {
                        when (resultCode) {
                            Activity.RESULT_OK -> data?.data?.let { setMockupPortrait(it) }
                                ?: showMessage("Cannot set portrait mockup")
                            Activity.RESULT_CANCELED -> Unit
                            else -> showMessage("Cannot set portrait mockup")
                        }
                    }
                    MockupOrientation.LANDSCAPE -> {
                        when (resultCode) {
                            Activity.RESULT_OK -> data?.data?.let { setMockupLandscape(it) }
                                ?: showMessage("Cannot set landscape mockup")
                            Activity.RESULT_CANCELED -> Unit
                            else -> showMessage("Cannot set landscape mockup")
                        }
                    }
                }
            }
        PermissionRequest(requestCode)
            ?.let { permission ->
                when (permission) {
                    PermissionRequest.OVERLAY -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (Settings.canDrawOverlays(this).not()) {
                                showMessage("Overlay permission denied")
                            } else {

                            }
                        } else {
                            // TODO: What about this?
                        }
                    }
                    PermissionRequest.MEDIA_PROJECTION -> {
                        val unit = when (resultCode) {
                            Activity.RESULT_OK -> {
                                MediaProjectionHelper.data = data
                                toggleMagnifier(true)
                            }
                            Activity.RESULT_CANCELED -> {
                                with(binding.overlayMagnifierView) {
                                    if (magnifierSwitch.isChecked) {
                                        magnifierSwitch.isChecked = false
                                    }
                                }
                            }
                            else -> {
                                // TODO: What about this?
                            }
                        }
                        unit
                    }
                }
            }
    }

    override fun setupUi(configuration: DesignerConfiguration) {
        (binding.toolbar.menu.findItem(R.id.status).actionView as? SwitchMaterial)?.let {
            it.setOnCheckedChangeListener(null)
            it.isChecked = configuration.enabled
            it.setOnCheckedChangeListener { _, isChecked ->
                when (isChecked) {
                    true -> createService()
                    false -> destroyService()
                }
            }
        }
        setupGridOverlay(configuration.grid)
        setupMockupOverlay(configuration.mockup)
        setupMagnifierOverlay(configuration.magnifier)
        toggleUi(configuration.enabled)
    }

    private fun setupToolbar() {
        with(binding) {
            toolbar.setNavigationOnClickListener { finish() }
        }
    }

    private fun toggleUi(enabled: Boolean) {
        with(binding) {

            with(overlayGridView) {
                gridOverlaySwitch.isEnabled = enabled

                horizontalLineColorButton.isEnabled = enabled
                verticalLineColorButton.isEnabled = enabled
                horizontalLineColorButton.alpha = if (enabled) 1.0f else 0.5f
                verticalLineColorButton.alpha = if (enabled) 1.0f else 0.5f

                decreaseHorizontalGridSizeButton.isEnabled = enabled
                increaseHorizontalGridSizeButton.isEnabled = enabled
                horizontalGridSizeSlider.isEnabled = enabled

                decreaseVerticalGridSizeButton.isEnabled = enabled
                increaseVerticalGridSizeButton.isEnabled = enabled
                verticalGridSizeSlider.isEnabled = enabled
            }

            with(overlayMockupView) {
                mockupOverlaySwitch.isEnabled = enabled

                decreaseMockupOpacityButton.isEnabled = enabled
                increaseMockupOpacityButton.isEnabled = enabled
                mockupOpacitySlider.isEnabled = enabled

                portraitMockup.isEnabled = enabled
                landscapeMockup.isEnabled = enabled

                clearPortraitMockupButton.isEnabled = enabled
                clearLandscapeMockupButton.isEnabled = enabled
            }

            with(overlayMagnifierView) {
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
    }

    private fun setupGridOverlay(configuration: GridConfiguration) =
        with(binding.overlayGridView) {
            gridOverlaySwitch.setOnCheckedChangeListener(null)
            gridOverlaySwitch.isChecked = configuration.enabled
            gridOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                toggleGrid(isChecked)
            }

            horizontalLineColorButton.setOnClickListener {
                openGridColorPicker(LineOrientation.HORIZONTAL, configuration)
            }
            verticalLineColorButton.setOnClickListener {
                openGridColorPicker(LineOrientation.VERTICAL, configuration)
            }

            horizontalGridSizeSlider.valueFrom = 1.0f
            horizontalGridSizeSlider.value =
                configuration.horizontalGridSize.toFloat() / resources.displayMetrics.density
            horizontalGridSizeSlider.valueTo =
                floor(resources.displayMetrics.heightPixels / resources.displayMetrics.density / 2.0f).roundToInt()
                    .toFloat()
            decreaseHorizontalGridSizeButton.setOnClickListener {
                horizontalGridSizeSlider.value =
                    (horizontalGridSizeSlider.value - horizontalGridSizeSlider.stepSize).coerceAtLeast(
                        horizontalGridSizeSlider.valueFrom
                    )
            }
            increaseHorizontalGridSizeButton.setOnClickListener {
                horizontalGridSizeSlider.value =
                    (horizontalGridSizeSlider.value + horizontalGridSizeSlider.stepSize).coerceAtMost(
                        horizontalGridSizeSlider.valueTo
                    )
            }
            horizontalGridSizeSlider.clearOnChangeListeners()
            horizontalGridSizeSlider.addOnChangeListener { _, value, _ ->
                updateGridHorizontalGap(value.toPx())
                horizontalGridSizeValueLabel.text = "${value.roundToInt()}dp"
            }

            verticalGridSizeSlider.valueFrom = 1.0f
            verticalGridSizeSlider.value =
                configuration.verticalGridSize.toFloat() / resources.displayMetrics.density
            verticalGridSizeSlider.valueTo =
                floor(resources.displayMetrics.widthPixels / resources.displayMetrics.density / 2.0f).roundToInt()
                    .toFloat()
            decreaseVerticalGridSizeButton.setOnClickListener {
                verticalGridSizeSlider.value =
                    (verticalGridSizeSlider.value - verticalGridSizeSlider.stepSize).coerceAtLeast(
                        verticalGridSizeSlider.valueFrom
                    )
            }
            increaseVerticalGridSizeButton.setOnClickListener {
                verticalGridSizeSlider.value =
                    (verticalGridSizeSlider.value + verticalGridSizeSlider.stepSize).coerceAtMost(
                        verticalGridSizeSlider.valueTo
                    )
            }
            verticalGridSizeSlider.clearOnChangeListeners()
            verticalGridSizeSlider.addOnChangeListener { _, value, _ ->
                updateGridVerticalGap(value.toPx())
                verticalGridSizeValueLabel.text = "${value.roundToInt()}dp"
            }
            horizontalLineColorButton.setBackgroundColor(configuration.horizontalLineColor)
            verticalLineColorButton.setBackgroundColor(configuration.verticalLineColor)
            horizontalLineColorValueLabel.text =
                "${configuration.horizontalLineColor.getHexCode()}"
            vertialLineColorValueLabel.text = "${configuration.verticalLineColor.getHexCode()}"
            horizontalGridSizeValueLabel.text = "${horizontalGridSizeSlider.value.roundToInt()}dp"
            verticalGridSizeValueLabel.text = "${verticalGridSizeSlider.value.roundToInt()}dp"
        }

    private fun setupMockupOverlay(configuration: MockupConfiguration) =
        with(binding.overlayMockupView) {
            mockupOverlaySwitch.setOnCheckedChangeListener(null)
            mockupOverlaySwitch.isChecked = configuration.enabled
            mockupOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                toggleMockup(isChecked)
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
                updateMockupOpacity(value / 100.0f)
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

    private fun setupMagnifierOverlay(configuration: MagnifierConfiguration) =
        with(binding.overlayMagnifierView) {
            magnifierSwitch.setOnCheckedChangeListener(null)
            magnifierSwitch.isChecked = configuration.enabled
            magnifierSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startProjection()
                } else {
                    toggleMagnifier(isChecked)
                }
            }
            colorModelToggleGroup.clearOnButtonCheckedListeners()
            colorModelToggleGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                updateMagnifierColorModel(
                    when (checkedId) {
                        R.id.hexButton -> ColorModel.HEX
                        R.id.rgbButton -> ColorModel.RGB
                        R.id.hsvButton -> ColorModel.HSV
                        else -> ColorModel.HEX
                    }
                )
            }
        }

    private fun setupPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this).not()) {
                startActivityForResult(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    ),
                    PermissionRequest.OVERLAY.requestCode
                )
            }
        } else {
            // TODO: what about this?
        }
    }

    private fun openGridColorPicker(
        orientation: LineOrientation,
        configuration: GridConfiguration
    ) {
        DesignerViewColorPickerBinding.inflate(layoutInflater)
            .apply {
                colorPickerView.setColorListener(object : ColorEnvelopeListener {
                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        envelope?.let {
                            when (orientation) {
                                LineOrientation.HORIZONTAL -> setHorizontalGridLineColor(
                                    it.color,
                                    it.hexCode
                                )
                                LineOrientation.VERTICAL -> setVerticalGridLineColor(
                                    it.color,
                                    it.hexCode
                                )
                            }
                        }
                    }
                })
                when (orientation) {
                    LineOrientation.HORIZONTAL -> colorPickerView.fireColorListener(
                        configuration.horizontalLineColor,
                        false
                    )
                    LineOrientation.VERTICAL -> colorPickerView.fireColorListener(
                        configuration.verticalLineColor,
                        false
                    )
                }

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

    private fun setHorizontalGridLineColor(
        color: Int,
        code: String
    ) {
        with(binding.overlayGridView) {
            horizontalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            horizontalLineColorValueLabel.text = "#${code.drop(2)}"
        }

        updateGridHorizontalColor(color)
    }

    private fun setVerticalGridLineColor(
        color: Int,
        code: String
    ) {
        with(binding.overlayGridView) {
            verticalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            vertialLineColorValueLabel.text = "#${code.drop(2)}"
        }

        updateGridVerticalColor(color)
    }

    private fun setMockupPortrait(uri: Uri) {
        with(binding.overlayMockupView) {
            portraitMockup.setImageURI(uri)
            clearPortraitMockupButton.isVisible = true
        }
        updateMockupPortraitUri(uri.toString())
    }

    private fun setMockupLandscape(uri: Uri) {
        with(binding.overlayMockupView) {
            landscapeMockup.setImageURI(uri)
            clearLandscapeMockupButton.isVisible = true
        }
        updateMockupLandscapeUri(uri.toString())
    }

    private fun clearPortraitMockup() {
        with(binding.overlayMockupView) {
            if (portraitMockup.drawable != null) {
                portraitMockup.setImageDrawable(null)
                clearPortraitMockupButton.isGone = true
                showMessage("Portrait mockup cleared")
            }
        }
        updateMockupPortraitUri(null)
    }

    private fun clearLandscapeMockup() {
        with(binding.overlayMockupView) {
            if (landscapeMockup.drawable != null) {
                landscapeMockup.setImageDrawable(null)
                clearLandscapeMockupButton.isGone = true
                showMessage("Landscape mockup cleared")
            }
        }
        updateMockupLandscapeUri(null)
    }

    private fun showMessage(text: String) =
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()

    private fun startProjection() {
        startActivityForResult(
            (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).createScreenCaptureIntent(),
            PermissionRequest.MEDIA_PROJECTION.requestCode
        )
    }
}