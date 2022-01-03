package com.infinum.sentinel.ui.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.google.android.material.switchmaterial.SwitchMaterial
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.models.memory.triggers.TriggerType
import com.infinum.sentinel.databinding.SentinelFragmentSettingsBinding
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import kotlin.math.roundToInt
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SettingsFragment : BaseChildFragment<Nothing, SettingsEvent>(R.layout.sentinel_fragment_settings) {

    companion object {
        fun newInstance() = SettingsFragment()
        const val TAG: String = "SettingsFragment"

        private const val FORMAT_BUNDLE_SIZE = "%s kB"
    }

    override val binding: SentinelFragmentSettingsBinding by viewBinding(
        SentinelFragmentSettingsBinding::bind
    )

    override val viewModel: SettingsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { requireActivity().finish() }
            formatGroup.setOnCheckedChangeListener { _, checkedId ->
                viewModel.saveFormats(
                    listOf(
                        FormatEntity(
                            id = FormatType.PLAIN.ordinal.toLong(),
                            type = FormatType.PLAIN,
                            selected = R.id.plainChip == checkedId
                        ),
                        FormatEntity(
                            id = FormatType.MARKDOWN.ordinal.toLong(),
                            type = FormatType.MARKDOWN,
                            selected = R.id.markdownChip == checkedId
                        ),
                        FormatEntity(
                            id = FormatType.JSON.ordinal.toLong(),
                            type = FormatType.JSON,
                            selected = R.id.jsonChip == checkedId
                        ),
                        FormatEntity(
                            id = FormatType.XML.ordinal.toLong(),
                            type = FormatType.XML,
                            selected = R.id.xmlChip == checkedId
                        ),
                        FormatEntity(
                            id = FormatType.HTML.ordinal.toLong(),
                            type = FormatType.HTML,
                            selected = R.id.htmlChip == checkedId
                        )
                    )
                )
            }
            decreaseLimitButton.setOnClickListener {
                limitSlider.value = (limitSlider.value - limitSlider.stepSize).coerceAtLeast(limitSlider.valueFrom)
            }
            increaseLimitButton.setOnClickListener {
                limitSlider.value = (limitSlider.value + limitSlider.stepSize).coerceAtMost(limitSlider.valueTo)
            }
        }
    }

    override fun onState(state: Nothing) = Unit

    @Suppress("ComplexMethod", "LongMethod")
    override fun onEvent(event: SettingsEvent) =
        when (event) {
            is SettingsEvent.TriggersChanged -> {
                event.value.forEach { trigger ->
                    when (trigger.type) {
                        TriggerType.MANUAL -> setupSwitch(binding.manualTriggerView, trigger)
                        TriggerType.SHAKE -> setupSwitch(binding.shakeTriggerView, trigger)
                        TriggerType.FOREGROUND -> setupSwitch(binding.foregroundTriggerView, trigger)
                        TriggerType.USB_CONNECTED -> setupSwitch(binding.usbTriggerView, trigger)
                        TriggerType.AIRPLANE_MODE_ON -> setupSwitch(
                            binding.airplaneModeTriggerView,
                            trigger
                        )
                        else -> throw NotImplementedError()
                    }
                }
            }
            is SettingsEvent.FormatChanged -> {
                when (event.value.type) {
                    FormatType.PLAIN -> R.id.plainChip
                    FormatType.MARKDOWN -> R.id.markdownChip
                    FormatType.JSON -> R.id.jsonChip
                    FormatType.XML -> R.id.xmlChip
                    FormatType.HTML -> R.id.htmlChip
                    else -> throw NotImplementedError()
                }.let {
                    binding.formatGroup.check(it)
                }
            }
            is SettingsEvent.BundleMonitorChanged -> {
                binding.bundleMonitorSwitch.setOnCheckedChangeListener(null)
                binding.bundleMonitorSwitch.isChecked = event.value.notify
                binding.bundleMonitorSwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateBundleMonitor(event.value.copy(notify = isChecked))
                }

                binding.activityIntentExtrasChip.setOnCheckedChangeListener(null)
                binding.activitySavedStateChip.setOnCheckedChangeListener(null)
                binding.fragmentArgumentsChip.setOnCheckedChangeListener(null)
                binding.fragmentSavedStateChip.setOnCheckedChangeListener(null)
                binding.activityIntentExtrasChip.isChecked = event.value.activityIntentExtras
                binding.activitySavedStateChip.isChecked = event.value.activitySavedState
                binding.fragmentArgumentsChip.isChecked = event.value.fragmentArguments
                binding.fragmentSavedStateChip.isChecked = event.value.fragmentSavedState
                binding.activityIntentExtrasChip.setOnCheckedChangeListener { _, _ ->
                    viewModel.updateBundleMonitor(
                        event.value.copy(activityIntentExtras = binding.activityIntentExtrasChip.isChecked)
                    )
                }
                binding.activitySavedStateChip.setOnCheckedChangeListener { _, _ ->
                    viewModel.updateBundleMonitor(
                        event.value.copy(activitySavedState = binding.activitySavedStateChip.isChecked)
                    )
                }
                binding.fragmentArgumentsChip.setOnCheckedChangeListener { _, _ ->
                    viewModel.updateBundleMonitor(
                        event.value.copy(fragmentArguments = binding.fragmentArgumentsChip.isChecked)
                    )
                }
                binding.fragmentSavedStateChip.setOnCheckedChangeListener { _, _ ->
                    viewModel.updateBundleMonitor(
                        event.value.copy(fragmentSavedState = binding.fragmentSavedStateChip.isChecked)
                    )
                }

                binding.limitSlider.clearOnChangeListeners()
                binding.limitSlider.value = event.value.limit.toFloat()
                binding.limitSlider.addOnChangeListener { _, value, _ ->
                    binding.limitValueView.text = String.format(FORMAT_BUNDLE_SIZE, value.roundToInt())
                    viewModel.updateBundleMonitor(event.value.copy(limit = value.roundToInt()))
                }
                binding.limitValueView.text = String.format(FORMAT_BUNDLE_SIZE, event.value.limit)
            }
            is SettingsEvent.CrashMonitorChanged -> {
                binding.uncaughtExceptionSwitch.setOnCheckedChangeListener(null)
                binding.uncaughtExceptionSwitch.isChecked = event.value.notifyExceptions
                binding.uncaughtExceptionSwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateCrashMonitor(event.value.copy(notifyExceptions = isChecked))
                }
                binding.anrSwitch.setOnCheckedChangeListener(null)
                binding.anrSwitch.isChecked = event.value.notifyAnrs
                binding.anrSwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateCrashMonitor(event.value.copy(notifyAnrs = isChecked))
                }
            }
        }

    private fun setupSwitch(switchView: SwitchMaterial, trigger: TriggerEntity) =
        with(switchView) {
            isChecked = trigger.enabled
            isEnabled = trigger.editable
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleTrigger(
                    trigger.copy(enabled = isChecked)
                )
            }
        }
}
