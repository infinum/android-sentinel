package com.infinum.designer.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import android.provider.Settings
import android.util.TypedValue
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
import com.infinum.designer.ui.commander.DesignerCommander
import com.infinum.designer.ui.models.GridConfiguration
import com.infinum.designer.ui.models.LineOrientation
import com.infinum.designer.ui.models.MockupConfiguration
import com.infinum.designer.ui.models.MockupOrientation
import com.infinum.designer.ui.models.PermissionRequest
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.util.*
import kotlin.math.roundToInt


@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DesignerActivity : FragmentActivity() {

    private lateinit var binding: DesignerActivityDesignerBinding

    private var commander: DesignerCommander? = null

    private var bound: Boolean = false

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            commander =
                DesignerCommander(
                    Messenger(
                        service
                    )
                )
            commander?.bound = true
            bound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            bound = false
            commander?.bound = false
            commander = null
        }
    }

    private var gridConfiguration: GridConfiguration = GridConfiguration()
    private var mockupConfiguration: MockupConfiguration = MockupConfiguration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DesignerActivityDesignerBinding.inflate(layoutInflater)
            .also { setContentView(it.root) }
            .also { binding = it }
            .also {
                setupToolbar()
                setupGridOverlay()
                setupMockupOverlay()
                setupColorPicker()
                setupPermission()
                startService()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        MockupOrientation(requestCode)
            ?.let { orientation ->
                when (orientation) {
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
                }
            }
        PermissionRequest(requestCode)
            ?.let { permission ->
                when (permission) {
                    PermissionRequest.OVERLAY -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (Settings.canDrawOverlays(this).not()) {
                                showMessage("Overlay permission denied")
                            }
                        } else {
                            // TODO: What about this?
                        }
                    }
                }
            }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupGridOverlay() =
        with(binding) {
            gridOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                commander?.updateGrid(gridConfiguration.toBundle())
                commander?.toggleGrid(isChecked)
            }
            horizontalLineColorButton.setOnClickListener {
                openGridColorPicker(LineOrientation.HORIZONTAL)
            }
            verticalLineColorButton.setOnClickListener {
                openGridColorPicker(LineOrientation.VERTICAL)
            }
            horizontalGridSizeSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    gridConfiguration =
                        gridConfiguration.copy(horizontalGridSize = value.dpToPx(this@DesignerActivity))
                    commander?.updateGrid(gridConfiguration.toBundle())
                }

                horizontalGridSizeValueLabel.text = "${value.roundToInt()}dp"
            }
            verticalGridSizeSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    gridConfiguration =
                        gridConfiguration.copy(verticalGridSize = value.dpToPx(this@DesignerActivity))
                    commander?.updateGrid(gridConfiguration.toBundle())
                }

                verticalGridSizeValueLabel.text = "${value.roundToInt()}dp"
            }

            gridConfiguration = gridConfiguration.copy(
                horizontalLineColor = Color.RED,
                verticalLineColor = Color.BLUE,
                horizontalGridSize = horizontalGridSizeSlider.value.dpToPx(this@DesignerActivity),
                verticalGridSize = verticalGridSizeSlider.value.dpToPx(this@DesignerActivity)
            )
            horizontalLineColorButton.setBackgroundColor(gridConfiguration.horizontalLineColor)
            verticalLineColorButton.setBackgroundColor(gridConfiguration.verticalLineColor)
            horizontalLineColorValueLabel.text =
                "#${gridConfiguration.horizontalLineColor.getHexCode()}"
            vertialLineColorValueLabel.text = "#${gridConfiguration.verticalLineColor.getHexCode()}"
            horizontalGridSizeValueLabel.text = "${horizontalGridSizeSlider.value.roundToInt()}dp"
            verticalGridSizeValueLabel.text = "${verticalGridSizeSlider.value.roundToInt()}dp"
            commander?.updateGrid(gridConfiguration.toBundle())
        }

    private fun setupMockupOverlay() =
        with(binding) {
            mockupOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                commander?.updateMockup(mockupConfiguration.toBundle())
                commander?.toggleMockup(isChecked)
            }
            mockupOpacitySlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    mockupConfiguration = mockupConfiguration.copy(opacity = value)
                    commander?.updateMockup(mockupConfiguration.toBundle())
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
            mockupConfiguration = mockupConfiguration.copy(
                opacity = mockupOpacitySlider.value
            )
            mockupOpacityValueLabel.text = "${mockupConfiguration.opacity.roundToInt()}%"
            commander?.updateMockup(mockupConfiguration.toBundle())
        }

    private fun setupColorPicker() =
        with(binding) {
            colorPickerSwitch.setOnCheckedChangeListener { _, isChecked ->
                commander?.toggleColorPicker(isChecked)
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

    private fun openGridColorPicker(orientation: LineOrientation) {
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
                        gridConfiguration.horizontalLineColor,
                        false
                    )
                    LineOrientation.VERTICAL -> colorPickerView.fireColorListener(
                        gridConfiguration.verticalLineColor,
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

    private fun setHorizontalGridLineColor(color: Int, code: String) {
        with(binding) {
            horizontalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            horizontalLineColorValueLabel.text = "#${code.drop(2)}"
        }
        gridConfiguration = gridConfiguration.copy(horizontalLineColor = color)
        commander?.updateGrid(gridConfiguration.toBundle())
    }

    private fun setVerticalGridLineColor(color: Int, code: String) {
        with(binding) {
            verticalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            vertialLineColorValueLabel.text = "#${code.drop(2)}"
        }
        gridConfiguration = gridConfiguration.copy(verticalLineColor = color)
        commander?.updateGrid(gridConfiguration.toBundle())
    }

    private fun setMockupPortrait(uri: Uri) {
        with(binding) {
            portraitMockup.setImageURI(uri)
            clearPortraitMockupButton.isVisible = true
        }
        mockupConfiguration = mockupConfiguration.copy(portraitUri = uri.toString())
        commander?.updateMockup(mockupConfiguration.toBundle())
    }

    private fun setMockupLandscape(uri: Uri) {
        with(binding) {
            landscapeMockup.setImageURI(uri)
            clearLandscapeMockupButton.isVisible = true
        }
        mockupConfiguration = mockupConfiguration.copy(landscapeUri = uri.toString())
        commander?.updateMockup(mockupConfiguration.toBundle())
    }

    private fun clearPortraitMockup() {
        with(binding) {
            if (portraitMockup.drawable != null) {
                portraitMockup.setImageDrawable(null)
                clearPortraitMockupButton.isGone = true
                showMessage("Portrait mockup cleared")
            }
        }
        mockupConfiguration = mockupConfiguration.copy(portraitUri = null)
        commander?.updateMockup(mockupConfiguration.toBundle())
    }

    private fun clearLandscapeMockup() {
        with(binding) {
            if (landscapeMockup.drawable != null) {
                landscapeMockup.setImageDrawable(null)
                clearLandscapeMockupButton.isGone = true
                showMessage("Landscape mockup cleared")
            }
        }
        mockupConfiguration = mockupConfiguration.copy(landscapeUri = null)
        commander?.updateMockup(mockupConfiguration.toBundle())
    }

    private fun showMessage(text: String) =
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()

    private fun startService() {
        bindService(
            Intent(this, DesignerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun stopService() {
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
    }
}


fun Float.dpToPx(context: Context): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics
    ).toInt()

fun Int.getHexCode(): String {
    val r = Color.red(this)
    val g = Color.green(this)
    val b = Color.blue(this)
    return String.format(Locale.getDefault(), "%02X%02X%02X", r, g, b)
}