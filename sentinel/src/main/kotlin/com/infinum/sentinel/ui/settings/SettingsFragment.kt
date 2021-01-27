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
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SettingsFragment : BaseChildFragment(R.layout.sentinel_fragment_settings) {

    companion object {
        fun newInstance() = SettingsFragment()
        val TAG: String = SettingsFragment::class.java.simpleName
    }

    override val binding: SentinelFragmentSettingsBinding by viewBinding(
        SentinelFragmentSettingsBinding::bind
    )

    override val viewModel: SettingsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupTriggers()
        setupFormats()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { requireActivity().finish() }
    }

    private fun setupTriggers() {
        viewModel.triggers {
            it.forEach { trigger ->
                when (trigger.type) {
                    TriggerType.MANUAL -> setupSwitch(binding.manualTriggerView, trigger)
                    TriggerType.SHAKE -> setupSwitch(binding.shakeTriggerView, trigger)
                    TriggerType.FOREGROUND -> setupSwitch(binding.foregroundTriggerView, trigger)
                    TriggerType.USB_CONNECTED -> setupSwitch(binding.usbTriggerView, trigger)
                    TriggerType.AIRPLANE_MODE_ON -> setupSwitch(
                        binding.airplaneModeTriggerView,
                        trigger
                    )
                }
            }
        }
    }

    private fun setupFormats() {
        viewModel.formats { entity ->
            when (entity.type) {
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

        binding.formatGroup.setOnCheckedChangeListener { _, checkedId ->
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
