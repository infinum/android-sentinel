package com.infinum.sentinel.ui.main.preferences

import android.content.Intent
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.databinding.SentinelFragmentPreferencesBinding
import com.infinum.sentinel.databinding.SentinelViewItemPreferenceBinding
import com.infinum.sentinel.databinding.SentinelViewItemTextBinding
import com.infinum.sentinel.extensions.copyToClipboard
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.main.preferences.editor.PreferenceEditorActivity
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferencesFragment :
    BaseChildFragment<PreferencesState, Nothing>(R.layout.sentinel_fragment_preferences) {

    companion object {
        fun newInstance() = PreferencesFragment()
        const val TAG: String = "PreferencesFragment"
    }

    override val binding: SentinelFragmentPreferencesBinding by viewBinding(
        SentinelFragmentPreferencesBinding::bind
    )

    override val viewModel: PreferencesViewModel by viewModel()

    override fun onState(state: PreferencesState) =
        when (state) {
            is PreferencesState.Data -> with(binding) {
                contentLayout.removeAllViews()
                state.value.forEach {
                    contentLayout.addView(createItemView(it))
                }
            }
        }

    override fun onEvent(event: Nothing) = Unit

    private fun createItemView(data: PreferencesData): View =
        SentinelViewItemPreferenceBinding.inflate(layoutInflater, binding.contentLayout, false)
            .apply {
                nameView.text = data.name
                data.values.forEach { tuple ->
                    prefsLayout.addView(
                        SentinelViewItemTextBinding.inflate(layoutInflater, prefsLayout, false)
                            .apply {
                                labelView.isAllCaps = false
                                labelView.text = tuple.second
                                valueView.text = tuple.third.toString()
                                root.setOnClickListener { view ->
                                    view.context.startActivity(
                                        Intent(activity, PreferenceEditorActivity::class.java)
                                            .apply {
                                                putExtra(Presentation.Constants.KEY_PREFERENCE_FILE, data.name)
                                                putExtra(
                                                    Presentation.Constants.KEY_PREFERENCE_TYPE,
                                                    tuple.first.ordinal
                                                )
                                                putExtra(Presentation.Constants.KEY_PREFERENCE_KEY, tuple.second)
                                                putExtra(
                                                    Presentation.Constants.KEY_PREFERENCE_VALUE,
                                                    when (tuple.first) {
                                                        PreferenceType.BOOLEAN -> tuple.third as Boolean
                                                        PreferenceType.FLOAT -> tuple.third as Float
                                                        PreferenceType.INT -> tuple.third as Int
                                                        PreferenceType.LONG -> tuple.third as Long
                                                        PreferenceType.STRING -> tuple.third as String
                                                        PreferenceType.SET -> (tuple.third as Set<*>).toTypedArray()
                                                        else ->
                                                            throw IllegalArgumentException("Unknown preference type.")
                                                    }
                                                )
                                            }
                                    )
                                }
                                root.setOnLongClickListener {
                                    it.context.copyToClipboard(
                                        key = tuple.second,
                                        value = tuple.third.toString()
                                    )
                                }
                            }.root
                    )
                }
            }.root
}
