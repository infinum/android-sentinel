package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.models.memory.triggers.TriggerType
import com.infinum.sentinel.databinding.SentinelFragmentSettingsBinding
import com.infinum.sentinel.domain.repository.FormatsRepository
import com.infinum.sentinel.domain.repository.TriggersRepository
import com.infinum.sentinel.ui.shared.BaseChildFragment
import org.koin.android.ext.android.get

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
            val triggersRepository: TriggersRepository = get()
            triggersRepository.load().observeForever { triggers ->
                triggers.forEach { trigger ->
                    when (trigger.type) {
                        TriggerType.MANUAL -> {
                            with(manualTriggerView) {
                                isChecked = trigger.enabled
                                isEnabled = trigger.editable
                                setOnCheckedChangeListener { _, isChecked ->
                                    triggersRepository.save(trigger.copy(enabled = isChecked))
                                }
                            }
                        }
                        TriggerType.SHAKE -> {
                            with(shakeTriggerView) {
                                isChecked = trigger.enabled
                                isEnabled = trigger.editable
                                setOnCheckedChangeListener { _, isChecked ->
                                    triggersRepository.save(trigger.copy(enabled = isChecked))
                                }
                            }
                        }
                        TriggerType.FOREGROUND -> {
                            with(foregroundTriggerView) {
                                isChecked = trigger.enabled
                                isEnabled = trigger.editable
                                setOnCheckedChangeListener { _, isChecked ->
                                    triggersRepository.save(trigger.copy(enabled = isChecked))
                                }
                            }
                        }
                        TriggerType.USB_CONNECTED -> {
                            with(usbTriggerView) {
                                isChecked = trigger.enabled
                                isEnabled = trigger.editable
                                setOnCheckedChangeListener { _, isChecked ->
                                    triggersRepository.save(trigger.copy(enabled = isChecked))
                                }
                            }
                        }
                        TriggerType.AIRPLANE_MODE_ON -> {
                            with(airplaneModeTriggerView) {
                                isChecked = trigger.enabled
                                isEnabled = trigger.editable
                                setOnCheckedChangeListener { _, isChecked ->
                                    triggersRepository.save(trigger.copy(enabled = isChecked))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupFormats() {
        with(viewBinding) {
            val formatsRepository: FormatsRepository = get()
            formatGroup.setOnCheckedChangeListener { _, checkedId ->
                formatsRepository.save(
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
            formatsRepository.load().observeForever { entity ->
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
}
