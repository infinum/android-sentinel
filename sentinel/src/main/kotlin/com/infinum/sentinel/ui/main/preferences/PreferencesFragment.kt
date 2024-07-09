package com.infinum.sentinel.ui.main.preferences

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.databinding.SentinelFragmentPreferencesBinding
import com.infinum.sentinel.databinding.SentinelViewItemPreferenceBinding
import com.infinum.sentinel.databinding.SentinelViewItemTextBinding
import com.infinum.sentinel.extensions.copyToClipboard
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.main.preferences.editor.PreferenceEditorContract
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferencesFragment :
    BaseChildFragment<PreferencesState, PreferencesEvent>(R.layout.sentinel_fragment_preferences) {

    companion object {
        fun newInstance() = PreferencesFragment()
        const val TAG: String = "PreferencesFragment"
    }

    private lateinit var contract: ActivityResultLauncher<Unit>

    override val binding: SentinelFragmentPreferencesBinding by viewBinding(
        SentinelFragmentPreferencesBinding::bind
    )

    override val viewModel: PreferencesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contract = registerForActivityResult(PreferenceEditorContract()) { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.data()
            }
        }
    }

    override fun onDestroyView() {
        contract.unregister()
        super.onDestroyView()
    }

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
                contract.launch(Unit)
            }
        }

    private fun createItemView(data: PreferencesData): View =
        SentinelViewItemPreferenceBinding.inflate(layoutInflater, binding.contentLayout, false)
            .apply {
                nameView.text = data.name
                sortImageView.setOnClickListener {
                    viewModel.onSortClicked(data)
                }
                hideExpandImageView.setOnClickListener {
                    viewModel.onHideExpandClicked(data)
                }

                if (data.isExpanded) {
                    prefsLayout.visibility = View.VISIBLE
                    hideExpandImageView.setImageResource(R.drawable.sentinel_ic_minus)
                    showPreferenceData(data)
                } else {
                    prefsLayout.visibility = View.GONE
                    hideExpandImageView.setImageResource(R.drawable.sentinel_ic_plus)
                }
            }.root

    private fun SentinelViewItemPreferenceBinding.showPreferenceData(data: PreferencesData) {
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
    }
}
