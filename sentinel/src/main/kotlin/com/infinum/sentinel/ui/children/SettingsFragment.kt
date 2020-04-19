package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.sources.local.room.repository.FormatsRepository
import com.infinum.sentinel.data.sources.local.room.repository.TriggersRepository
import com.infinum.sentinel.databinding.SentinelFragmentChildSettingsBinding
import com.infinum.sentinel.databinding.SentinelItemTriggerBinding

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
        val TAG: String = SettingsFragment::class.java.simpleName
    }

    private var viewBinding: SentinelFragmentChildSettingsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = SentinelFragmentChildSettingsBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding?.let {
            TriggersRepository.load().observeForever { triggers ->
                it.triggersLayout.removeAllViews()
                triggers.forEach { trigger ->
                    it.triggersLayout.addView(
                        SentinelItemTriggerBinding.inflate(
                            LayoutInflater.from(it.triggersLayout.context),
                            it.triggersLayout,
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
            it.formatGroup.setOnCheckedChangeListener { _, checkedId ->
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
                it.formatGroup.check(id)
            }
        }
    }

    override fun onDestroy() =
        super.onDestroy().run {
            viewBinding = null
        }
}