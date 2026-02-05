package com.infinum.sentinel.ui.networkemulator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.infinum.sentinel.domain.networkemulator.NetworkEmulatorPreferences
import com.infinum.sentinel.tool.networkemulator.R
import com.infinum.sentinel.tool.networkemulator.databinding.SentinelFragmentNetworkEmulatorBinding

/**
 * Fragment that displays network emulator configuration controls.
 * Allows users to configure delay, failure rate, and variance for network emulation.
 */
public class NetworkEmulatorFragment : Fragment() {
    public companion object {
        public const val TAG: String = "NetworkEmulatorFragment"

        public fun newInstance(): NetworkEmulatorFragment = NetworkEmulatorFragment()
    }

    private var viewBinding: SentinelFragmentNetworkEmulatorBinding? = null
    private val binding get() = viewBinding!!

    @Suppress("LateinitUsage")
    private lateinit var preferences: NetworkEmulatorPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = SentinelFragmentNetworkEmulatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        preferences = NetworkEmulatorPreferences(requireContext())

        setupToolbar()
        setupControls()
        loadSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    private fun setupToolbar() {
        with(binding) {
            toolbar.setNavigationOnClickListener {
                activity?.finish()
            }
        }
    }

    private fun setupControls() {
        with(binding) {
            // Enable/disable switch
            enableSwitch.setOnCheckedChangeListener { _, isChecked ->
                preferences.isEnabled = isChecked
                updateControlsEnabled(isChecked)
            }

            // Delay slider
            delaySlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    preferences.delayMs = value.toInt()
                    updateDelayText(value.toInt())
                }
            }

            // Fail percentage slider
            failPercentageSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    preferences.failPercentage = value.toInt()
                    updateFailPercentageText(value.toInt())
                }
            }

            // Variance percentage slider
            variancePercentageSlider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    preferences.variancePercentage = value.toInt()
                    updateVariancePercentageText(value.toInt())
                }
            }

            // Reset button
            resetButton.setOnClickListener {
                resetSettings()
            }
        }
    }

    private fun loadSettings() {
        with(binding) {
            enableSwitch.isChecked = preferences.isEnabled
            delaySlider.value = preferences.delayMs.toFloat()
            failPercentageSlider.value = preferences.failPercentage.toFloat()
            variancePercentageSlider.value = preferences.variancePercentage.toFloat()

            updateDelayText(preferences.delayMs)
            updateFailPercentageText(preferences.failPercentage)
            updateVariancePercentageText(preferences.variancePercentage)
            updateControlsEnabled(preferences.isEnabled)
        }
    }

    private fun resetSettings() {
        preferences.reset()
        loadSettings()
    }

    @Suppress("MagicNumber", "CognitiveComplexMethod")
    private fun updateControlsEnabled(enabled: Boolean) {
        with(binding) {
            delaySlider.isEnabled = enabled
            failPercentageSlider.isEnabled = enabled
            variancePercentageSlider.isEnabled = enabled
            delayLabel.alpha = if (enabled) 1.0f else 0.5f
            failPercentageLabel.alpha = if (enabled) 1.0f else 0.5f
            variancePercentageLabel.alpha = if (enabled) 1.0f else 0.5f
            delayValueText.alpha = if (enabled) 1.0f else 0.5f
            failPercentageValueText.alpha = if (enabled) 1.0f else 0.5f
            variancePercentageValueText.alpha = if (enabled) 1.0f else 0.5f
        }
    }

    private fun updateDelayText(delayMs: Int) {
        binding.delayValueText.text = getString(R.string.sentinel_network_delay_value, delayMs)
    }

    private fun updateFailPercentageText(percentage: Int) {
        binding.failPercentageValueText.text =
            getString(R.string.sentinel_network_fail_percentage_value, percentage)
    }

    private fun updateVariancePercentageText(percentage: Int) {
        binding.variancePercentageValueText.text =
            getString(R.string.sentinel_network_variance_percentage_value, percentage)
    }
}
