package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import com.google.android.material.switchmaterial.SwitchMaterial
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.models.memory.triggers.TriggerType
import com.infinum.sentinel.databinding.SentinelFragmentSettingsBinding
import com.infinum.sentinel.ui.DependencyGraph
import com.infinum.sentinel.ui.shared.BaseChildFragment

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SettingsFragment : BaseChildFragment<SentinelFragmentSettingsBinding>() {

    companion object {
        fun newInstance() = SettingsFragment()
        val TAG: String = SettingsFragment::class.java.simpleName
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SentinelFragmentSettingsBinding =
        SentinelFragmentSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTriggers()
        setupFormats()
    }

    private fun setupTriggers() {
        with(viewBinding) {
            DependencyGraph.triggers().load().observeForever { triggers ->
                triggers.forEach { trigger ->
                    when (trigger.type) {
                        TriggerType.MANUAL -> setupSwitch(manualTriggerView, trigger)
                        TriggerType.SHAKE -> setupSwitch(shakeTriggerView, trigger)
                        TriggerType.FOREGROUND -> setupSwitch(foregroundTriggerView, trigger)
                        TriggerType.USB_CONNECTED -> setupSwitch(usbTriggerView, trigger)
                        TriggerType.AIRPLANE_MODE_ON -> setupSwitch(
                            airplaneModeTriggerView,
                            trigger
                        )
                    }
                }
            }
        }
    }

    private fun setupFormats() {
        with(viewBinding) {
            formatGroup.setOnCheckedChangeListener { _, checkedId ->
                DependencyGraph.formats().save(
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
            DependencyGraph.formats().load().observeForever { entity ->
                when (entity.type) {
                    FormatType.PLAIN -> R.id.plainChip
                    FormatType.MARKDOWN -> R.id.markdownChip
                    FormatType.JSON -> R.id.jsonChip
                    FormatType.XML -> R.id.xmlChip
                    FormatType.HTML -> R.id.htmlChip
                    else -> R.id.plainChip
                }.let {
                    formatGroup.check(it)
                }
            }
        }
    }

    private fun setupSwitch(switchView: SwitchMaterial, trigger: TriggerEntity) {
        with(switchView) {
            isChecked = trigger.enabled
            isEnabled = trigger.editable
            setOnCheckedChangeListener { _, isChecked ->
                DependencyGraph.triggers().save(trigger.copy(enabled = isChecked))
            }
        }
    }
}
