package com.infinum.designer.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import android.provider.Settings
import android.view.View
import androidx.annotation.RestrictTo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.infinum.designer.R
import com.infinum.designer.databinding.DesignerActivityDesignerBinding
import com.infinum.designer.databinding.DesignerViewColorPickerBinding
import com.infinum.designer.extensions.toPx
import com.infinum.designer.extensions.getHexCode
import com.infinum.designer.ui.commander.service.ServiceCommander
import com.infinum.designer.ui.commander.ui.UiCommandHandler
import com.infinum.designer.ui.commander.ui.UiCommandListener
import com.infinum.designer.ui.models.ColorModel
import com.infinum.designer.ui.models.LineOrientation
import com.infinum.designer.ui.models.MockupOrientation
import com.infinum.designer.ui.models.PermissionRequest
import com.infinum.designer.ui.models.ServiceAction
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
internal class DesignerActivity : FragmentActivity() {

    private lateinit var binding: DesignerActivityDesignerBinding

    private var commander: ServiceCommander? = null

    private var bound: Boolean = false

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            commander =
                ServiceCommander(
                    Messenger(service),
                    Messenger(
                        UiCommandHandler(
                            UiCommandListener(
                                onRegister = this@DesignerActivity::onRegister,
                                onUnregister = this@DesignerActivity::onUnregister
                            )
                        )
                    )
                )
            bound = true
            commander?.bound = bound

            commander?.register()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            bound = false
            commander?.bound = bound
            commander = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DesignerActivityDesignerBinding.inflate(layoutInflater)
            .also { setContentView(it.root) }
            .also { binding = it }
            .also {
                with(binding) {
                    horizontalGridSizeSlider.isEnabled = false
                    verticalGridSizeSlider.isEnabled = false
                    mockupOpacitySlider.isEnabled = false
                    portraitMockup.isEnabled = false
                    landscapeMockup.isEnabled = false
                }
                setupToolbar()
                setupUi(DesignerConfiguration())
                setupPermission()
            }
    }

    override fun onStart() {
        super.onStart()
        bindService()
    }

    override fun onStop() {
        super.onStop()
        unbindService()
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
                                commander?.toggleColorPicker(true)
                            }
                            Activity.RESULT_CANCELED -> {
                                with(binding) {
                                    if (colorPickerSwitch.isChecked) {
                                        colorPickerSwitch.isChecked = false
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

    private fun setupToolbar() {
        with(binding) {
            toolbar.setNavigationOnClickListener { finish() }
        }
    }

    private fun setupUi(configuration: DesignerConfiguration) {
        (binding.toolbar.menu.findItem(R.id.status).actionView as? SwitchMaterial)?.let {
            it.setOnCheckedChangeListener(null)
            it.isChecked = configuration.enabled
            it.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startService()
                    bindService()
                } else {
                    commander?.unregister()
                }
            }
        }
        setupGridOverlay(configuration.grid)
        setupMockupOverlay(configuration.mockup)
        setupColorPicker(configuration.magnifier)
        toggleUi(configuration.enabled)
    }

    private fun toggleUi(enabled: Boolean) {
        with(binding) {
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

            mockupOverlaySwitch.isEnabled = enabled

            decreaseMockupOpacityButton.isEnabled = enabled
            increaseMockupOpacityButton.isEnabled = enabled
            mockupOpacitySlider.isEnabled = enabled

            portraitMockup.isEnabled = enabled
            landscapeMockup.isEnabled = enabled

            clearPortraitMockupButton.isEnabled = enabled
            clearLandscapeMockupButton.isEnabled = enabled

            colorPickerSwitch.isEnabled = enabled
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

    private fun setupGridOverlay(configuration: GridConfiguration) =
        with(binding) {
            gridOverlaySwitch.setOnCheckedChangeListener(null)
            gridOverlaySwitch.isChecked = configuration.enabled
            gridOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                commander?.toggleGrid(isChecked)
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
                commander?.updateGridHorizontalGap(
                    bundleOf("horizontalGridSize" to value.toPx())
                )
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
                commander?.updateGridVerticalGap(
                    bundleOf("verticalGridSize" to value.toPx())
                )
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
        with(binding) {
            mockupOverlaySwitch.setOnCheckedChangeListener(null)
            mockupOverlaySwitch.isChecked = configuration.enabled
            mockupOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                commander?.toggleMockup(isChecked)
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
                commander?.updateMockupOpacity(
                    bundleOf("opacity" to value)
                )
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

    private fun setupColorPicker(configuration: MagnifierConfiguration) =
        with(binding) {
            colorPickerSwitch.setOnCheckedChangeListener(null)
            colorPickerSwitch.isChecked = configuration.enabled
            colorPickerSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startProjection()
                } else {
                    commander?.toggleColorPicker(isChecked)
                }
            }
            colorModelToggleGroup.clearOnButtonCheckedListeners()
            colorModelToggleGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                commander?.updateColorPickerColorMode(
                    bundleOf(
                        "colorModel" to when (checkedId) {
                            R.id.hexButton -> ColorModel.HEX
                            R.id.rgbButton -> ColorModel.RGB
                            R.id.hsvButton -> ColorModel.HSV
                            else -> ColorModel.HEX
                        }.type
                    )
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

    private fun onRegister(bundle: Bundle) {
        setupUi(bundle.getParcelable("configuration") ?: DesignerConfiguration())
    }

    private fun onUnregister(bundle: Bundle) {
        setupUi(bundle.getParcelable("configuration") ?: DesignerConfiguration())
        unbindService()
        stopService()
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
        with(binding) {
            horizontalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            horizontalLineColorValueLabel.text = "#${code.drop(2)}"
        }

        commander?.updateGridHorizontalColor(
            bundleOf("horizontalLineColor" to color)
        )
    }

    private fun setVerticalGridLineColor(
        color: Int,
        code: String
    ) {
        with(binding) {
            verticalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            vertialLineColorValueLabel.text = "#${code.drop(2)}"
        }
        commander?.updateGridVerticalColor(
            bundleOf("verticalLineColor" to color)
        )
    }

    private fun setMockupPortrait(uri: Uri) {
        with(binding) {
            portraitMockup.setImageURI(uri)
            clearPortraitMockupButton.isVisible = true
        }
        commander?.updateMockupPortraitUri(
            bundleOf("portraitUri" to uri.toString())
        )
    }

    private fun setMockupLandscape(uri: Uri) {
        with(binding) {
            landscapeMockup.setImageURI(uri)
            clearLandscapeMockupButton.isVisible = true
        }
        commander?.updateMockupLandscapeUri(
            bundleOf("landscapeUri" to uri.toString())
        )
    }

    private fun clearPortraitMockup() {
        with(binding) {
            if (portraitMockup.drawable != null) {
                portraitMockup.setImageDrawable(null)
                clearPortraitMockupButton.isGone = true
                showMessage("Portrait mockup cleared")
            }
        }
        commander?.updateMockupPortraitUri(
            bundleOf("portraitUri" to null)
        )
    }

    private fun clearLandscapeMockup() {
        with(binding) {
            if (landscapeMockup.drawable != null) {
                landscapeMockup.setImageDrawable(null)
                clearLandscapeMockupButton.isGone = true
                showMessage("Landscape mockup cleared")
            }
        }
        commander?.updateMockupLandscapeUri(
            bundleOf("landscapeUri" to null)
        )
    }

    private fun showMessage(text: String) =
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()

    private fun startService() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, DesignerService::class.java).apply {
                action = ServiceAction.START.code
            }
        )
    }

    private fun stopService() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, DesignerService::class.java).apply {
                action = ServiceAction.STOP.code
            }
        )
    }

    private fun bindService() {
        if (bound.not()) {
            bindService(
                Intent(this, DesignerService::class.java),
                serviceConnection,
                0
            )
        } else {
            commander?.register()
        }
    }

    private fun unbindService() {
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
    }

    private fun startProjection() {
        startActivityForResult(
            (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).createScreenCaptureIntent(),
            PermissionRequest.MEDIA_PROJECTION.requestCode
        )
    }
}