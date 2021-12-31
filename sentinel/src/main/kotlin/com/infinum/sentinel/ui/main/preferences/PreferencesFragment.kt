package com.infinum.sentinel.ui.main.preferences

import android.content.Intent
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.databinding.SentinelFragmentPreferencesBinding
import com.infinum.sentinel.databinding.SentinelViewItemPreferenceBinding
import com.infinum.sentinel.databinding.SentinelViewItemTextBinding
import com.infinum.sentinel.extensions.copyToClipboard
import com.infinum.sentinel.ui.main.preferences.editor.PreferenceEditorActivity
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferencesFragment :
    BaseChildFragment<PreferencesState, PreferencesEvent>(R.layout.sentinel_fragment_preferences) {

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

    override fun onEvent(event: PreferencesEvent) =
        when (event) {
            is PreferencesEvent.Cached -> {
                startActivity(
                    Intent(activity, PreferenceEditorActivity::class.java)
                )
            }
        }

    @Suppress("UNCHECKED_CAST")
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
                                root.setOnClickListener { _ ->
                                    viewModel.cache(
                                        data.name,
                                        tuple
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
