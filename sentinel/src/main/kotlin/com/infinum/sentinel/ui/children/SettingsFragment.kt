package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.sources.local.room.repository.FormatsRepository
import com.infinum.sentinel.data.sources.local.room.repository.TriggersRepository
import com.infinum.sentinel.databinding.SentinelFragmentSettingsBinding
import com.infinum.sentinel.databinding.SentinelViewItemCheckboxBinding
import com.infinum.sentinel.ui.shared.BaseChildFragment

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SettingsFragment : BaseChildFragment<SentinelFragmentSettingsBinding>() {

    companion object {
        fun newInstance() = SettingsFragment()
        val TAG: String = SettingsFragment::class.java.simpleName
    }

    override fun provideViewBinding(): SentinelFragmentSettingsBinding =
        SentinelFragmentSettingsBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTriggers()
        setupFormats()
    }

    private fun setupTriggers() {
        with(viewBinding) {
            TriggersRepository.load().observeForever { triggers ->
                triggersLayout.removeAllViews()
                triggers.forEach { trigger ->
                    triggersLayout.addView(
                        SentinelViewItemCheckboxBinding.inflate(
                            layoutInflater,
                            triggersLayout,
                            false
                        )
                            .apply {
                                triggerCheckBox.text =
                                    trigger.type?.name.orEmpty().toLowerCase().capitalize()
                                        .replace("_", " ")
                                triggerCheckBox.isChecked = trigger.enabled
                                triggerCheckBox.isEnabled = trigger.editable
                                triggerCheckBox.setOnCheckedChangeListener { _, isChecked ->
                                    TriggersRepository.save(trigger.copy(enabled = isChecked))
                                }
                            }.root
                    )
                }
            }
        }
    }

    private fun setupFormats() {
        with(viewBinding) {
            formatGroup.setOnCheckedChangeListener { _, checkedId ->
                FormatsRepository.save(
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
            FormatsRepository.load().observeForever { entity ->
                val id = when (entity.type) {
                    FormatType.PLAIN -> R.id.plainChip
                    FormatType.MARKDOWN -> R.id.markdownChip
                    FormatType.JSON -> R.id.jsonChip
                    FormatType.XML -> R.id.xmlChip
                    FormatType.HTML -> R.id.htmlChip
                    else -> R.id.plainChip
                }
                formatGroup.check(id)
            }
        }
    }
}
