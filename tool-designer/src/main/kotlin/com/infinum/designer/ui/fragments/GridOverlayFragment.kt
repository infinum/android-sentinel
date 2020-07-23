package com.infinum.designer.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.infinum.designer.R
import com.infinum.designer.databinding.DesignerFragmentOverlayGridBinding
import com.infinum.designer.databinding.DesignerViewColorPickerBinding
import com.infinum.designer.extensions.getHexCode
import com.infinum.designer.extensions.toPx
import com.infinum.designer.ui.models.LineOrientation
import com.infinum.designer.ui.models.configuration.GridConfiguration
import com.infinum.designer.ui.utils.viewBinding
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlin.math.floor
import kotlin.math.roundToInt

internal class GridOverlayFragment :
    AbstractOverlayFragment<GridConfiguration>(R.layout.designer_fragment_overlay_grid) {

    override val binding: DesignerFragmentOverlayGridBinding by viewBinding(
        DesignerFragmentOverlayGridBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            horizontalGridSizeSlider.isEnabled = false
            verticalGridSizeSlider.isEnabled = false
        }
    }

    override fun toggleUi(enabled: Boolean) {
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
        }
    }

    override fun configure(configuration: GridConfiguration) {
        with(binding) {
            gridOverlaySwitch.setOnCheckedChangeListener(null)
            gridOverlaySwitch.isChecked = configuration.enabled
            gridOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                serviceActivity?.toggleGrid(isChecked)
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
                serviceActivity?.updateGridHorizontalGap(value.toPx())
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
                serviceActivity?.updateGridVerticalGap(value.toPx())
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
                MaterialAlertDialogBuilder(it.root.context)
                    .setTitle("Select ${orientation.name.toLowerCase()} line color")
                    .setView(it.root)
                    .setNegativeButton("Close") { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
    }

    private fun setHorizontalGridLineColor(color: Int, code: String) {
        with(binding) {
            horizontalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            horizontalLineColorValueLabel.text = "#${code.drop(2)}"
        }

        serviceActivity?.updateGridHorizontalColor(color)
    }

    private fun setVerticalGridLineColor(color: Int, code: String) {
        with(binding) {
            verticalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            vertialLineColorValueLabel.text = "#${code.drop(2)}"
        }

        serviceActivity?.updateGridVerticalColor(color)
    }
}