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
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SettingsFragment : BaseChildFragment<Nothing, SettingsEvent>(R.layout.sentinel_fragment_settings) {

    companion object {
        fun newInstance() = SettingsFragment()
        const val TAG: String = "SettingsFragment"

        private const val FORMAT_BUNDLE_SIZE = "%s kB"
        private const val FORMAT_CERTIFICATE_TO_EXPIRE = "%s %s"
    }

    override val binding: SentinelFragmentSettingsBinding by viewBinding(
        SentinelFragmentSettingsBinding::bind
    )

    override val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { requireActivity().finish() }
            formatGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                viewModel.saveFormats(
                    listOf(
                        FormatEntity(
                            id = FormatType.PLAIN.ordinal.toLong(),
                            type = FormatType.PLAIN,
                            selected = checkedIds.contains(R.id.plainChip)
                        ),
                        FormatEntity(
                            id = FormatType.MARKDOWN.ordinal.toLong(),
                            type = FormatType.MARKDOWN,
                            selected = checkedIds.contains(R.id.markdownChip)
                        ),
                        FormatEntity(
                            id = FormatType.JSON.ordinal.toLong(),
                            type = FormatType.JSON,
                            selected = checkedIds.contains(R.id.jsonChip)
                        ),
                        FormatEntity(
                            id = FormatType.XML.ordinal.toLong(),
                            type = FormatType.XML,
                            selected = checkedIds.contains(R.id.xmlChip)
                        ),
                        FormatEntity(
                            id = FormatType.HTML.ordinal.toLong(),
                            type = FormatType.HTML,
                            selected = checkedIds.contains(R.id.htmlChip)
                        )
                    )
                )
            }
            decreaseLimitButton.setOnClickListener {
                limitSlider.value = (limitSlider.value - limitSlider.stepSize)
                    .coerceAtLeast(limitSlider.valueFrom)
            }
            increaseLimitButton.setOnClickListener {
                limitSlider.value = (limitSlider.value + limitSlider.stepSize)
                    .coerceAtMost(limitSlider.valueTo)
            }
            decreaseToExpireButton.setOnClickListener {
                toExpireAmountSlider.value =
                    (toExpireAmountSlider.value - toExpireAmountSlider.stepSize)
                        .coerceAtLeast(toExpireAmountSlider.valueFrom)
            }
            increaseToExpireButton.setOnClickListener {
                toExpireAmountSlider.value =
                    (toExpireAmountSlider.value + toExpireAmountSlider.stepSize)
                        .coerceAtMost(toExpireAmountSlider.valueTo)
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
                        TriggerType.PROXIMITY -> setupSwitch(binding.proximityTriggerView, trigger)
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
                binding.includeAllDataSwitch.setOnCheckedChangeListener(null)
                binding.includeAllDataSwitch.isChecked = event.value.includeAllData
                binding.includeAllDataSwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateCrashMonitor(event.value.copy(includeAllData = isChecked))
                }
            }
            is SettingsEvent.CertificateMonitorChanged -> {
                binding.runOnStartSwitch.setOnCheckedChangeListener(null)
                binding.runOnStartSwitch.isChecked = event.value.runOnStart
                binding.runOnStartSwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateCertificatesMonitor(event.value.copy(runOnStart = isChecked))
                }
                binding.runInBackgroundSwitch.setOnCheckedChangeListener(null)
                binding.runInBackgroundSwitch.isChecked = event.value.runInBackground
                binding.runInBackgroundSwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateCertificatesMonitor(event.value.copy(runInBackground = isChecked))
                }
                binding.checkInvalidNowSwitch.setOnCheckedChangeListener(null)
                binding.checkInvalidNowSwitch.isChecked = event.value.notifyInvalidNow
                binding.checkInvalidNowSwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateCertificatesMonitor(event.value.copy(notifyInvalidNow = isChecked))
                }
                binding.checkToExpireSwitch.setOnCheckedChangeListener(null)
                binding.checkToExpireSwitch.isChecked = event.value.notifyToExpire
                binding.checkToExpireSwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateCertificatesMonitor(event.value.copy(notifyToExpire = isChecked))
                }
                binding.toExpireAmountSlider.clearOnChangeListeners()
                binding.toExpireAmountSlider.value = event.value.expireInAmount.toFloat()
                binding.toExpireAmountSlider.addOnChangeListener { _, value, _ ->
                    binding.toExpireValueView.text = String.format(
                        FORMAT_CERTIFICATE_TO_EXPIRE,
                        value.roundToInt(),
                        event.value.expireInUnit.name.lowercase()
                    )
                    viewModel.updateCertificatesMonitor(event.value.copy(expireInAmount = value.roundToInt()))
                }
                binding.toExpireValueView.text = String.format(
                    FORMAT_CERTIFICATE_TO_EXPIRE,
                    event.value.expireInAmount,
                    event.value.expireInUnit.name.lowercase()
                )
                when (event.value.expireInUnit) {
                    ChronoUnit.DAYS -> binding.daysButton.isChecked = true
                    ChronoUnit.WEEKS -> binding.weeksButton.isChecked = true
                    ChronoUnit.MONTHS -> binding.monthsButton.isChecked = true
                    ChronoUnit.YEARS -> binding.yearsButton.isChecked = true
                    else -> binding.daysButton.isChecked = true
                }
                binding.daysButton.setOnClickListener {
                    viewModel.updateCertificatesMonitor(event.value.copy(expireInUnit = ChronoUnit.DAYS))
                }
                binding.weeksButton.setOnClickListener {
                    viewModel.updateCertificatesMonitor(event.value.copy(expireInUnit = ChronoUnit.WEEKS))
                }
                binding.monthsButton.setOnClickListener {
                    viewModel.updateCertificatesMonitor(event.value.copy(expireInUnit = ChronoUnit.MONTHS))
                }
                binding.yearsButton.setOnClickListener {
                    viewModel.updateCertificatesMonitor(event.value.copy(expireInUnit = ChronoUnit.YEARS))
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
